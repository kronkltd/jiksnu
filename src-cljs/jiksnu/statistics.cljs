(ns jiksnu.statistics
  (:use [jayq.core :only [$]])
  (:require [jayq.core :as jayq]
            [goog.string :as gstring]
            [goog.string.format :as gformat]
            [jiksnu.logging :as jl]
            [jiksnu.model :as model]
            [jiksnu.util.ko :as ko]
            [lolg :as log]))

(def *logger* (log/get-logger "jiksnu.statistics"))

;; Depricated
(defn update-statistics
  [stats]
  (let [stat-section ($ :.statistics-section)]
    (doseq [key (keys stats)]
      (let [finder (gstring/format "*[data-model='%s']" key)]
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
  (let [stat-url "/main/stats.json"]
    (.getJSON js/jQuery stat-url (partial update-statistics-handler model))))
