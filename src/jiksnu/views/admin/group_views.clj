(ns jiksnu.views.admin.group-views
  (:use [ciste.sections.default :only [index-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.group-actions :only [index]])
  (:require [jiksnu.sections.group-sections :as sections.like]))

(defview #'index :html
  [request {:keys [items]}]
  {:single true
   :title "Groups"
   :body (index-section items)})
