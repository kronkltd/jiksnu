(ns jiksnu.actions.salmon-actions
  (:use (ciste [core :only (defaction)]
               [debug :only (spy)])
        clojure.contrib.core)
  (:require (jiksnu [abdera :as abdera])
            (jiksnu.actions [activity-actions :as actions.activity])
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

(defn decode-envelope
  [envelope]
  (->> envelope
       (.decodeData default-sig)
       String.))

(defn extract-activity
  [envelope]
  (-?> envelope
       decode-envelope
       abdera/parse-xml-string
       actions.activity/entry->activity))

(defn stream->envelope
  "convert an input stream to an envelope"
  [input-stream]
  (.deserialize deserializer input-stream))

(defaction process
  [stream]
  (let [envelope (stream->envelope stream)]
    (if-let [activity (extract-activity envelope)]
      (if-let [key (-?> activity
                        actions.activity/get-author
                        get-key)]
        (if (signature-valid? envelope key)
          (actions.activity/remote-create activity))))))
