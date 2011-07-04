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

(defaction process
  [request]
  (let [deserializer (com.cliqset.magicsig.xml.XMLMagicEnvelopeDeserializer.)
        default-sig (MagicSig/getDefault)
        salmon (Salmon/getDefault)
        body (:body request)
        envelope (.deserialize deserializer body)
        data-bytes (.decodeData default-sig envelope)
        uri (.getSignerUri (SimpleAtomDataParser.) data-bytes)
        data (spy (String. data-bytes))]
    (if-let [entry (abdera/parse-xml-string data)]
      (let [activity (to-activity (spy entry))]
        (spy activity)
        (if-let [author (.getAuthor (spy entry))]
         (let [author-uri (.getUri (spy author))
               user (model.user/find-or-create-by-remote-id (str author-uri))
               key-pair (model.signature/get-key-for-user (spy user))
               key (model.signature/get-key-from-armored (spy key-pair))
               signatures (.getSignatures envelope)
               verification-result (.verify default-sig envelope
                                            (list (spy key)))
               result-data (.getData (spy verification-result))
               sig-results (.getSignatureVerificationResults
                            verification-result)]
           (if (spy (some identity
                       (map
                        (fn [r] (.isVerified r))
                        sig-results)))
             (actions.activity/remote-create [activity]))))))))

