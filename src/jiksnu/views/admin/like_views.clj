(ns jiksnu.views.admin.like-views
  (:use [ciste.views :only [defview]]
        [jiksnu.actions.admin.like-actions :only [index]]
        [jiksnu.sections :only [admin-index-section]])
  (:require [jiksnu.sections.like-sections :as sections.like]))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:single true
   :title "Likes"
   :body (admin-index-section items response)})
