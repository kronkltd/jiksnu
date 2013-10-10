(ns jiksnu.actions.salmon-actions
  (:require [ciste.config :refer [config]]
            [ciste.core :refer [defaction]]
            [ciste.model :as cm]
            [clojure.core.incubator :refer [-?> -?>>]]
            [clojure.tools.logging :as log]
            [jiksnu.modules.atom.util :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.key :as model.key]
            [slingshot.slingshot :refer [throw+]])
  (:import java.net.URI
           java.security.PublicKey
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.commons.codec.binary.Base64))

;; Can match be used here?
(defn normalize-user-id
  "If given a bare identifier, append the acct: scheme"
  [s]
  (or (if-let [m (re-matches #"(acct:)?(.+@.+)" s)]
        (when (empty? (second m))
          (str "acct:" s)))
      s))

(defn ^String decode-envelope
  "Decode the envelope of a envelope map"
  [envelope]
  (let [data (:data envelope)]
    (String. (Base64/decodeBase64 data) "UTF-8")))

(defn ^PublicKey get-key
  "Fetch the key for the user as a PublicKey"
  [^User author]
  (-?> author
       model.key/get-key-for-user
       model.key/get-key-from-armored))

(defn signature-valid?
  "Tests if the signature is valid for the key"
  [envelope ^PublicKey pub-key]
  (let [{:keys [data datatype encoding alg]} envelope
        sig (-> envelope :sig model.key/decode)
        bytes (.getBytes
               (model.key/get-base-string
                data
                (model.key/encode (.getBytes datatype "UTF-8"))
                (model.key/encode (.getBytes encoding "UTF-8"))
                (model.key/encode (.getBytes alg "UTF-8"))))]
    (model.key/verified? bytes sig pub-key)))

(defn ^Activity extract-activity
  "decode the data of the envelope and return the activity"
  [envelope]
  (-?> envelope
       decode-envelope
       abdera/parse-xml-string
       actions.activity/entry->activity))

(defn stream->envelope
  "Convert an InputStream to an envelope"
  ;; TODO: typehint
  [input-stream]
  (let [doc (cm/stream->document input-stream)
        root (.getRootElement doc)]
    (let [data-tag (first (cm/query root "//*[local-name()='data']"))]
      {:sig (.getValue (first (cm/query root "//*[local-name()='sig']")))
       :datatype (.getAttributeValue data-tag "type")
       :data (.getValue data-tag)
       :alg (.getValue (first (cm/query root "//*[local-name()='alg']")))
       :encoding (.getValue (first (cm/query root "//*[local-name()='encoding']")))})))

;; TODO: swap order of arguments
(defaction process
  "Process a salmon envelope in the context of a user"
  [user envelope]
  (if-let [activity (extract-activity envelope)]
    (if-let [actor (model.activity/get-author activity)]
      (if-let [pub-key (get-key actor)]
        (if (or (signature-valid? envelope pub-key)
                (when (not (config :salmon :verify))
                  (log/warn "bypassing salmon authentication")
                  true))
          (let [verb (:verb activity)
                object (:object activity)]
            (condp = verb
              "follow" (actions.subscription/subscribed actor user)
              "unfollow" (actions.subscription/unsubscribed actor user)
              "post" (actions.activity/remote-create [activity])
              (log/info (str "other: " verb))))
          (throw+ "signature is not valid"))
        (throw+ "Could not find key"))
      (throw+ "could not determine actor"))
    (throw+ "Could not extract activity")))
