(ns jiksnu.util
  (:require [lolg :as log]))

(def *logger* (log/get-logger "jiksnu.util"))

(defn fetch-model
  [model-name id callback]
  (let [url (format "/model/%s/%s.model" model-name id)]
    (log/finer *logger* (str "fetching " url))
    (.getJSON js/jQuery url callback)))

