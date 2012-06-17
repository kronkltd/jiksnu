(ns jiksnu.views.admin.user-views
  (:use [ciste.views :only [defview]]
        [jiksnu.actions.admin.user-actions :only [index]]
        [jiksnu.sections :only [admin-index-section]]))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:single true
   :title "Users"
   :body (admin-index-section items response)})
