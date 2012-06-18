(ns jiksnu.views.admin.activity-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [link-to]]
        [jiksnu.actions.admin.activity-actions :only [index]]
        [jiksnu.sections :only [admin-index-section]])
  (:require [jiksnu.actions.activity-actions :as actions.activity]))


(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Activities"
   :single true
   :body (admin-index-section items response)})
