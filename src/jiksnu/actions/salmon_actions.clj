(ns jiksnu.actions.salmon-actions
  (:use (ciste core debug)
        jiksnu.helpers.activity-helpers)
  (:require (clojure.java [io :as io])
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            (jiksnu.model [user :as model.user]
                          [signature :as model.signature]))
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
  ;; TODO: extract body in filter
  (let [body (:body request)]
    (with-open [input body]
      (println (line-seq (io/reader input))))
    (let [envelope (.deserialize deserializer body)
          data-bytes (.decodeData default-sig envelope)
          data (String. data-bytes)]
      (if-let [entry (abdera/parse-xml-string data)]
        (if-let [author (.getAuthor entry)]
          (let [key (get-key-for-author author)]
            (if (signature-valid? envelope key)
              (actions.activity/remote-create
               [(entry->activity entry)]))))))))

