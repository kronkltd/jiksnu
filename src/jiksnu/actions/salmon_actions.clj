(ns jiksnu.actions.salmon-actions
  (:use (ciste [config :only [config definitializer]]
               [core :only [defaction]]
               [debug :only [spy]])
        (clojure.core [incubator :only [-?> -?>>]]))
  (:require (clojure.tools [logging :as log])
            (jiksnu [abdera :as abdera])
            (jiksnu.actions [activity-actions :as actions.activity]
                            [subscription-actions :as actions.subscription]
                            [user-actions :as actions.user])
            (jiksnu.helpers [activity-helpers :as helpers.activity])
            (jiksnu.model [user :as model.user]
                          [signature :as model.signature])
            [saxon :as s])
  (:import jiksnu.model.User
           org.apache.commons.codec.binary.Base64))

(defn get-key
  [^User author]
  (-?> author
       model.signature/get-key-for-user
       model.signature/get-key-from-armored))

(defn signature-valid?
  [envelope pub-key]
  (let [data (-> envelope :data model.signature/decode)
        sig (-> envelope :sig model.signature/decode)]
    (model.signature/verified? data sig pub-key)))

(defn decode-envelope
  [envelope]
  (let [data (:data envelope)]
    (String. (Base64/decodeBase64 data))))

(defn extract-activity
  [envelope]
  (-?> envelope
       decode-envelope
       abdera/parse-xml-string
       actions.activity/entry->activity))

(defn stream->envelope
  "convert an input stream to an envelope"
  [input-stream]
  (let [doc (s/compile-xml input-stream)]
    {:sig (str (s/query "//*[local-name()='sig']/text()" doc))
     :data (str (s/query "//*[local-name()='data']/text()" doc))
     :alg (str (s/query "//*[local-name()='alg']/text()" doc))
     :encoding (str (s/query "//*[local-name()='encoding']/text()" doc))}))

(defaction process
  [user envelope]
  (if-let [activity (extract-activity envelope)]
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
