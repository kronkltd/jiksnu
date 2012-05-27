(ns jiksnu.views.admin.user-views
  (:use (ciste [views :only [defview]])
        jiksnu.actions.admin.user-actions)
  (:require (jiksnu.sections [user-sections :as sections.user])
            (jiksnu.helpers [user-helpers :as helpers.user])))

(defview #'index :html
  [request {:keys [items]}]
  {:single true
   :title "Users"
   :body
   [:div
    [:table.users.table
     [:thead
      [:tr
       [:th]
       [:th "User"]
       [:th "Domain"]
       [:th "Discover"]
       [:th "Update"]
       [:th "Edit"]
       [:th "Delete"]]]
     [:tbody
      (map sections.user/admin-index-line items)]]]})

