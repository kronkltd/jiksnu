(ns jiksnu.actions.salmon-actions
  (:use ciste.core
        ciste.debug
        jiksnu.helpers.activity-helpers)
  (:require [jiksnu.abdera :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.signature :as model.signature])
  (:import com.cliqset.salmon.Salmon
           com.cliqset.magicsig.dataparser.SimpleAtomDataParser
           com.cliqset.magicsig.MagicSig))

(defonce default-sig (MagicSig/getDefault))
(defonce salmon (Salmon/getDefault))
(defonce deserializer (com.cliqset.magicsig.xml.XMLMagicEnvelopeDeserializer.))

(defn get-key-for-author
  [author]
  (->> author .getUri str
       model.user/find-or-create-by-remote-id
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
  (let [body (:body request)
        envelope (.deserialize deserializer body)
        data-bytes (.decodeData default-sig envelope)
        data (String. data-bytes)]
    (if-let [entry (abdera/parse-xml-string data)]
      (if-let [author (.getAuthor entry)]
        (let [key (get-key-for-author author)]
          (if (signature-valid? envelope key)
            (actions.activity/remote-create
             [(entry->activity entry)])))))))

