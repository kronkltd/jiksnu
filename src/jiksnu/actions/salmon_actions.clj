(ns jiksnu.actions.salmon-actions
  (:use (ciste [config :only [config definitializer]]
               [core :only [defaction]]
               [debug :only [spy]])
        (clojure.core [incubator :only [-?> -?>>]]))
  (:require (clojure [string :as string])
            (clojure.tools [logging :as log])
            (jiksnu [abdera :as abdera]
                    [model :as model])
            (jiksnu.actions [activity-actions :as actions.activity]
                            [subscription-actions :as actions.subscription]
                            [user-actions :as actions.user])
            (jiksnu.model [user :as model.user]
                          [signature :as model.signature]))
  (:import java.security.PublicKey
           java.net.URI
           jiksnu.model.User
           org.apache.commons.codec.binary.Base64))

;; Can match be used here?
(defn normalize-user-id
  [s]
  (or (if-let [m (re-matches #"(acct:)?(.+@.+)" s)]
        (when (empty? (second m))
          (str "acct:" s)))
      s))

(defn ^PublicKey get-key
  [^User author]
  (-?> author
       model.signature/get-key-for-user
       model.signature/get-key-from-armored))

(defn signature-valid?
  "Tests if the signature is valid for the key"
  [envelope ^PublicKey pub-key]
  (let [{:keys [data datatype encoding alg]} (spy envelope)
        sig (-> envelope :sig model.signature/decode)
        bytes (.getBytes
               (model.signature/get-base-string
                data
                (model.signature/encode (.getBytes datatype "UTF-8"))
                (model.signature/encode (.getBytes encoding "UTF-8"))
                (model.signature/encode (.getBytes alg "UTF-8"))))]
    (model.signature/verified? bytes sig pub-key)))

(defn ^String decode-envelope
  [envelope]
  (let [data (:data envelope)]
    (String. (Base64/decodeBase64 data) "UTF-8")))

(defn extract-activity
  [envelope]
  (-?> envelope
       decode-envelope
       abdera/parse-xml-string
       actions.activity/entry->activity))

(defn stream->envelope
  "convert an input stream to an envelope"
  [input-stream]
  (let [doc (model/stream->document input-stream)]
    (let [data-tag (first (model/query "//*[local-name()='data']" doc))]
      {:sig (.getValue (first (model/query "//*[local-name()='sig']" doc)))
       :datatype (.getAttributeValue data-tag "type")
       :data (.getValue data-tag)
       :alg (.getValue (first (model/query "//*[local-name()='alg']" doc)))
       :encoding (.getValue (first (model/query "//*[local-name()='encoding']" doc)))})))

(defaction process
  [user envelope]
  (if-let [activity (extract-activity envelope)]
    (if-let [actor (actions.activity/get-author activity)]
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
          (throw (RuntimeException. "signature is not valid")))
        (throw (RuntimeException. "Could not find key")))
      (throw (RuntimeException. "could not determine actor")))
    (throw (RuntimeException. "Could not extract activity"))))

(definitializer
  (doseq [namespace ['jiksnu.filters.salmon-filters
                     'jiksnu.views.salmon-views]]
    (require namespace)))
