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
        data (String. data-bytes)]
    (if-let [entry (abdera/parse-xml-string data)]
      (let [activity (entry->activity entry)]
        (if-let [author (.getAuthor entry)]
          (let [author-uri (.getUri author)
                user (model.user/find-or-create-by-remote-id (str author-uri))
                key-pair (model.signature/get-key-for-user user)
                key (model.signature/get-key-from-armored key-pair)
                signatures (.getSignatures envelope)
                verification-result (.verify default-sig envelope
                                             (list key))
                result-data (.getData verification-result)
                sig-results (.getSignatureVerificationResults
                             verification-result)]
            (if (some identity
                      (map
                       (fn [r] (.isVerified r))
                       sig-results))
              (actions.activity/remote-create [activity]))))))))

