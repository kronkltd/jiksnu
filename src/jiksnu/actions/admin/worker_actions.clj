(ns jiksnu.actions.admin.worker-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]])
  (:require [ciste.workers :as workers]))

(defn get-worker-info
  [[id {:keys [name stopping host counter]}]]
  [name id stopping host counter])

(defaction index
  []
  (map get-worker-info @workers/*workers*))

(defaction start-worker
  "Start a worker with the given name"
  [name]
  (workers/start-worker! (keyword name))
  true)

(defaction stop-worker
  [id]
  (workers/stop-worker! id))

(defaction stop-all-workers
  "Stop all workers on this host"
  []
  (workers/stop-all-workers!)
  true)

(definitializer
  (require-namespaces
   ["jiksnu.views.admin.worker-views"
    "jiksnu.filters.admin.worker-filters"
    "jiksnu.sections.worker-sections"]))

