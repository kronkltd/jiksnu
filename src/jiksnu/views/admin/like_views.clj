(ns jiksnu.views.admin.like-views
  (:use [ciste.sections.default :only [index-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.like-actions :only [index]])
  (:require [jiksnu.sections.like-sections :as sections.like]))

(defview #'index :html
  [request {:keys [items] :as page}]
  {:single true
   :title "Likes"
   :body (index-section items page)})
