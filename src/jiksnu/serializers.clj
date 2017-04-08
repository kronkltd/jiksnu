(ns jiksnu.serializers
  (:require [clojure.data.json :as json])
  (:import clojure.lang.Var
           java.io.PrintWriter
           java.text.SimpleDateFormat
           java.util.Date
           org.bson.types.ObjectId))

(defn write-json-date
  ([^Date date ^PrintWriter out]
   (write-json-date date out false))
  ([^Date date ^PrintWriter out escape-unicode?]
   (let [formatted-date (.format (SimpleDateFormat. "yyyy-MM-dd'T'hh:mm:ss'Z'") date)]
     (.print out (str "\"" formatted-date "\"")))))

(defn write-json-object-id
  ([id ^PrintWriter out]
   (write-json-object-id id out false))
  ([id ^PrintWriter out escape-unicode]
   (.print out (str "\"" id "\""))))

(defn write-quoted
  ([o ^PrintWriter out]
   (write-quoted o out false))
  ([o ^PrintWriter out escape-unicode]
   (.print out (str "\"" o "\""))))

(extend Date json/JSONWriter {:-write write-json-date})
(extend ObjectId json/JSONWriter {:-write write-json-object-id})
(extend Var json/JSONWriter {:-write write-quoted})
