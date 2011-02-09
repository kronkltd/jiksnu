(ns jiksnu.core
  (:use clojure.contrib.json)
  (:require [jiksnu.http :as http]
            [jiksnu.xmpp :as xmpp])
  (:import java.util.Date))

(defn start
  ([] (start 8082))
  ([port]
     (http/start port)
     (xmpp/start)))

(defn -main
  []
  (start (Integer/parseInt 8082)))

(defn write-json-date [date out escape-unicode?]
  (.print out (.format
               (java.text.SimpleDateFormat. "yyyy-MMM-dd hh:mm:ss a")
            date)))

(extend Date Write-JSON
  {:write-json write-json-date})
