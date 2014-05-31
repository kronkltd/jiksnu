(ns jiksnu.util
  (:require [goog.string :as gstring]
            [goog.string.format :as gformat]
            [lolg :as log]))

(def *logger* (log/get-logger "jiksnu.util"))

(defn fetch-model
  [model-name id callback]
  (let [url (gstring/format "/model/%s/%s.model" model-name id)]
    (log/finer *logger* (str "fetching " url))
    (.getJSON js/jQuery url callback)))

