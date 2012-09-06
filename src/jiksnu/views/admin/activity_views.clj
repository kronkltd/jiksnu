(ns jiksnu.views.admin.activity-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [link-to]]
        [jiksnu.actions.admin.activity-actions :only [index]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [admin-index-section dump-data format-page-info]])
  (:require [jiksnu.actions.activity-actions :as actions.activity])
  (:import jiksnu.model.Activity))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Activities"
   :single true
   :viewmodel "/admin/activities.viewmodel"
   :body
   [:div (when *dynamic*
           {:data-bind "with: items"})
    (let [activities (if *dynamic*
                       [(Activity.)]
                       items)]
      (admin-index-section activities response))]})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  (doall
   {:body
    {:title "Activities"
     :pageInfo (format-page-info page)
     :items (map :_id items)
     :activities (admin-index-section items page)}}))
