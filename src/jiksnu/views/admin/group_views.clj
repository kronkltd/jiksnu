(ns jiksnu.views.admin.group-views
  (:use [ciste.sections.default :only [index-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.group-actions :only [index]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [admin-index-section format-page-info with-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.sections.group-sections :as sections.like])
  (:import jiksnu.model.Group))

(defview #'index :html
  [request {:keys [items] :as page}]
  {:single true
   :title "Groups"
   :viewmodel "/admin/groups.viewmodel"
   :body
   (with-page "default"
     [:div (if *dynamic*
             {:data-bind "with: items"})
      (let [items (if *dynamic* [(Group.)] items)]
        (admin-index-section items page))])})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Groups"
          :pages {:default (format-page-info page)}
          :groups (admin-index-section items page)}})
