(ns jiksnu.modules.admin.views.worker-views
  (:require [ciste.views :refer [defview]]
            [ciste.workers :as workers]
            [jiksnu.modules.admin.actions.worker-actions :as actions.worker]
            [jiksnu.modules.web.sections.worker-sections :as sections.worker]))

(defview #'actions.worker/index :html
  [request workers]
  {:title "Workers"
   :body
   (list
    (sections.worker/running-worker-section workers)
    (sections.worker/start-worker-form)
    #_(sections.worker/available-worker-section workers))})

(defview #'actions.worker/start-worker :html
  [request _]
  {:status 303
   :template false
   :flash "worker started"
   :headers {"Location" "/admin/workers"}})

(defview #'actions.worker/stop-all-workers :html
  [request _]
  {:status 303
   :template false
   :flash "all workers stopped"
   :headers {"Location" "/admin/workers"}})

(defview #'actions.worker/stop-worker :html
  [request _]
  {:status 303
   :template false
   :flash "Worker stopped"
   :headers {"Location" "/admin/workers"}})
