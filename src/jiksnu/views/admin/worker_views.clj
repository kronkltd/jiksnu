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

(defview #'start-worker :html
  [request _]
  {:status 303
   :template false
   :headers {"Location" "/"}})

(defview #'stop-all-workers :html
  [request _]
  {:status 303
   :template false
   :headers {"Location" "/"}})

(defview #'stop-worker :html
  [request _]
  {:status 303
   :template false
   :flash "Worker stopped"
   :headers {"Location" "/"}})

