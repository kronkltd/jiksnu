(ns jiksnu.core
  (:use clojure.contrib.json)
  (:require [jiksnu.http :as http]
            [jiksnu.xmpp :as xmpp])
  (:import java.text.SimpleDateFormat
           java.util.Date
           org.bson.types.ObjectId))

(def #^:dynamic *date-format* "yyyy-MM-dd'T'hh:mm:ssZ")

(defn start
  ([] (start 8082))
  ([port]
     (http/start port)
     (xmpp/start)))

(defn -main
  []
  (start (Integer/parseInt 8082)))

(defn write-json-date [date out escape-unicode?]
  (let [formatted-date (.format (SimpleDateFormat. *date-format*) date)]
    (.print out (str "\"" formatted-date "\""))))

(defn write-json-object-id
  [id out escape-unicode]
  (.print out (str "\"" id "\"")))

(extend Date Write-JSON
  {:write-json write-json-date})
(extend ObjectId Write-JSON
  {:write-json write-json-object-id})
