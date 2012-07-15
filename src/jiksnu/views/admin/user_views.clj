(ns jiksnu.views.admin.user-views
  (:use [ciste.sections.default :only [title]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.user-actions :only [index show]]
        [jiksnu.sections :only [admin-index-section admin-show-section]]))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:single true
   :title "Users"
   :body (admin-index-section items response)})

(defview #'show :html
  [request user]
  {:title (title user)
   :single true
   :body (admin-show-section user)})

