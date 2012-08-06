(ns jiksnu.views.admin.worker-views
  (:use [ciste.views :only [defview]]
        jiksnu.actions.admin.worker-actions)
  (:require [ciste.workers :as workers]
            [jiksnu.sections.worker-sections :as sections.worker]))

(defview #'index :html
  [request workers]
  {:title "Workers"
   :body
   (list
    (sections.worker/running-worker-section workers)
    (sections.worker/start-worker-form)
    #_(sections.worker/available-worker-section workers))})

(defview #'index :text
  [request workers]
  {:body
   (map
    #(pr-str %)
    workers)})

(defview #'index :json
  [request workers]
  {:body workers})

(defview #'start-worker :html
  [request _]
  {:status 303
   :template false
   :flash "worker started"
   :headers {"Location" "/admin/workers"}})

(defview #'start-worker :text
  [request data]
  {:body data})

(defview #'start-worker :json
  [request data]
  {:body data})





(defview #'stop-all-workers :html
  [request _]
  {:status 303
   :template false
   :flash "all workers stopped"
   :headers {"Location" "/admin/workers"}})

(defview #'stop-worker :html
  [request _]
  {:status 303
   :template false
   :flash "Worker stopped"
   :headers {"Location" "/admin/workers"}})

