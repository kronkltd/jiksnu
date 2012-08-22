(ns jiksnu.views.admin.group-views
  (:use [ciste.sections.default :only [index-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.group-actions :only [index]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [admin-index-section]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.sections.group-sections :as sections.like])
  (:import jiksnu.model.Group))

(defview #'index :html
  [request {:keys [items] :as page}]
  {:single true
   :title "Groups"
   :viewmodel "/admin/groups.viewmodel"
   :body
   [:div (if *dynamic*
           {:data-bind "with: _.map(items(), jiksnu.core.get_group)"})
    (let [items (if *dynamic* [(Group.)] items)]
      (admin-index-section items page))]})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Groups"
          :items (map :_id items)
          :groups (admin-index-section items page)}})
