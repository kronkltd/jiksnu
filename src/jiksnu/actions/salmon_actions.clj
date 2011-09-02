(ns jiksnu.actions.salmon-actions
  (:use (ciste [core :only (defaction)]
               [debug :only (spy)])
        clojure.contrib.core)
  (:require (clojure.java [io :as io])
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            (jiksnu.helpers [activity-helpers :as helpers.activity])
            (jiksnu.model [user :as model.user]
                          [signature :as model.signature]))
  (:import com.cliqset.salmon.Salmon
           com.cliqset.magicsig.dataparser.SimpleAtomDataParser
           com.cliqset.magicsig.MagicSig
           jiksnu.model.User))

(defonce default-sig (MagicSig/getDefault))
(defonce salmon (Salmon/getDefault))
(defonce deserializer (com.cliqset.magicsig.xml.XMLMagicEnvelopeDeserializer.))

(defn get-key
  [^User author]
  (-?> author
      model.signature/get-key-for-user
      model.signature/get-key-from-armored))

(defn signature-valid?
  [envelope key]
  (->> (.verify default-sig envelope (list key))
       .getSignatureVerificationResults
       (map #(.isVerified %))
       (some identity)))

(defaction process
  [request]
  ;; TODO: extract body in filter
  (let [body (:body request)]
    (let [envelope (.deserialize deserializer body)
          data-bytes (.decodeData default-sig envelope)
          data (String. data-bytes)]
      (if-let [entry (abdera/parse-xml-string data)]
        (if-let [activity (helpers.activity/entry->activity entry)]
          (let [author (actions.activity/get-author activity)]
            (if-let [key (get-key author)]
              (if (signature-valid? envelope key)
                (actions.activity/remote-create)))))))))
