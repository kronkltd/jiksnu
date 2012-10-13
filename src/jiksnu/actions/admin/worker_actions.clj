(ns jiksnu.actions.admin.worker-actions
  (:use [ciste.commands :only [add-command!]]
        [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.runner :only [require-namespaces]])
  (:require [ciste.workers :as workers]))

(defn get-worker-info
  [[id {:keys [name stopping host counter]}]]
  [name id stopping host counter])

(defaction index
  []
  (map get-worker-info @workers/*workers*))


;; TODO: Pass back the worker

(defaction start-worker
  "Start a worker with the given name"
  [name]
  (workers/start-worker! (keyword name))
  true)

;; (defn start-worker
;;   [& args]
;;   (:id (workers/start-worker! (keyword (first args)))))





(defaction stop-worker
  [id]
  (workers/stop-worker! id))

(defaction stop-all-workers
  "Stop all workers on this host"
  []
  (workers/stop-all-workers!)
  true)


(add-command! "start-worker" #'start-worker)
(add-command! "stop-worker" #'stop-worker)
(add-command! "list-workers" #'index)


(definitializer
  (require-namespaces
   ["jiksnu.views.admin.worker-views"
    "jiksnu.filters.admin.worker-filters"
    "jiksnu.sections.worker-sections"]))

