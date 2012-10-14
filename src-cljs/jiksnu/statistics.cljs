(ns jiksnu.statistics
  (:use [jayq.core :only [$]])
  (:require [jayq.core :as jayq]
            [jiksnu.ko :as ko]
            [jiksnu.logging :as log]
            [jiksnu.model :as model]))

;; Depricated
(defn update-statistics
  [stats]
  (let [stat-section ($ :.statistics-section)]
    (doseq [key (keys stats)]
      (let [finder (format "*[data-model='%s']" key)]
        (when-let [section (jayq/find stat-section finder)]
          (.effect section "highlight" 3000)
          (jayq/text (jayq/find section :.stat-value)
                     (get stats key)))))))

(defn update-statistics-handler
  [model data]
  (let [body (.-body data)]
    (.statistics model body)))

(defn fetch-statistics
  [model]
  (let [stat-url "http://renfer.name/main/stats.json"]
    (.getJSON js/jQuery stat-url (partial update-statistics-handler model))))
