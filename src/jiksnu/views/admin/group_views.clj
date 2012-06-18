(ns jiksnu.views.admin.group-views
  (:use [ciste.views :only [defview]]
        [jiksnu.actions.admin.group-actions :only [index]]
        [jiksnu.sections :only [admin-index-section]])
  (:require [jiksnu.sections.group-sections :as sections.like]))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:single true
   :title "Groups"
   :body (admin-index-section items response)})
