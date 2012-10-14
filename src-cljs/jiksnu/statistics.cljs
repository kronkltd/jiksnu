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
  (let [body (.-body data)
        b (ko/obj->model body (.-statistics model))]
    (.statistics model b)))

(defn fetch-statistics
  [model]
  (.getJSON js/jQuery
            "http://renfer.name/main/stats.json"
            (partial update-statistics-handler model))
  nil)

(defn mock-stats
  [model]
  (let [stats (model/Statistics.)]
    ;; TODO: use mapping plugin
    (doseq [[k v] {"users" 9001
                   "conversations" 2
                   "groups" 4
                   "domains" 6
                   "feedSources" 8
                   "feedSubscriptions" 16
                   "subscriptions" 32
                   "activities" "123456"}]
      (ko/assoc-observable stats k v))
    (ko/assoc-observable model "statistics" stats)))
