(ns jiksnu.actions.salmon-actions
  (:use (ciste [config :only [config definitializer]]
               [core :only [defaction]]
               [debug :only [spy]])
        (clojure.core [incubator :only [-?> -?>>]]))
  (:require (clojure.tools [logging :as log])
            (jiksnu [abdera :as abdera]
                    [model :as model])
            (jiksnu.actions [activity-actions :as actions.activity]
                            [subscription-actions :as actions.subscription]
                            [user-actions :as actions.user])
            (jiksnu.helpers [activity-helpers :as helpers.activity])
            (jiksnu.model [user :as model.user]
                          [signature :as model.signature]))
  (:import java.security.PublicKey
           jiksnu.model.User
           org.apache.commons.codec.binary.Base64))

(defn normalize-user-id
  [s]
  s
  )

(defn get-key
  [^User author]
  (-?> author
       model.signature/get-key-for-user
       model.signature/get-key-from-armored))

(defn signature-valid?
  [envelope ^PublicKey pub-key]
  (let [{:keys [data datatype encoding alg]} envelope
        sig (-> envelope :sig model.signature/decode)]
    (model.signature/verified?
     (.getBytes
      (spy (model.signature/get-base-string
            data
            (model.signature/encode (.getBytes datatype))
            (model.signature/encode (.getBytes encoding))
            (model.signature/encode (.getBytes alg)))))
     sig pub-key)))

(defn decode-envelope
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
  (let [doc (model/compile-xml input-stream)]
    (let [data-tag (first (model/query "//*[local-name()='data']" doc))]
      {:sig (.getValue (first (model/query "//*[local-name()='sig']" doc)))
       :datatype (.getAttributeValue data-tag "type")
       :data (.getValue data-tag)
       :alg (.getValue (first (model/query "//*[local-name()='alg']" doc)))
       :encoding (.getValue (first (model/query "//*[local-name()='encoding']" doc)))})))

(defaction process
  [user envelope]
  (if-let [activity (extract-activity (spy envelope))]
    (if-let [actor (helpers.activity/get-author activity)]
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
