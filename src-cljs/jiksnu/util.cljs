(ns jiksnu.util
  (:require [lolg :as log]))

(def *logger* (log/get-logger "jiksnu.util"))

(defn fetch-model
  [model-name id callback]
  (let [url (str "/model/" model-name "/" id ".model")]
    (log/finer *logger* (str "fetching " url))
    (.getJSON js/jQuery url callback)))

