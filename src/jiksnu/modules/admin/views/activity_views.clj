(ns jiksnu.modules.admin.views.activity-views
  (:use [ciste.views :only [defview]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.admin.actions.activity-actions :only [index]]
        [jiksnu.modules.core.sections :only [admin-index-section]]
        [jiksnu.modules.web.sections :only [bind-to dump-data format-page-info
                                            pagination-links with-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity])
  (:import jiksnu.model.Activity))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Activities"
   :single true
   :body
   (let [activities (if *dynamic* [(Activity.)] items)]
     (with-page "activities"
       (pagination-links response)
       (admin-index-section activities response)))})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  (doall
   {:body
    {:title "Activities"
     :pages {:activities (format-page-info page)}
     :activities (admin-index-section items page)}}))