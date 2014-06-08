(ns jiksnu.statistics
  (:require [dommy.core :as dommy]
            [jiksnu.logging :as jl]
            [jiksnu.model :as model]
            [jiksnu.util.ko :as ko]
            [lolg :as log])
  (:use-macros [dommy.macros :only [sel sel1]])
  )

(def *logger* (log/get-logger "jiksnu.statistics"))

;; Depricated
(defn update-statistics
  [stats]
  (let [stat-section (sel :.statistics-section)]
    (doseq [key (keys stats)]
      (let [finder (str "*[data-model='" key "']")]
        (when-let [section (sel stat-section finder)]
          (.effect section "highlight" 3000)
          (dommy/set-text! (sel section :.stat-value)
                           (get stats key)))))))

(defn update-statistics-handler
  [model data]
  (let [body (.-body data)]
    (.statistics model body)))

(defn fetch-statistics
  [model]
  (let [stat-url "/main/stats.json"]
    (.getJSON js/jQuery stat-url (partial update-statistics-handler model))))
