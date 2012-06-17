(ns jiksnu.views.group-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section add-form]]
        jiksnu.actions.group-actions)
  (:require [clojure.tools.logging :as log]))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Groups"
   :body
   [:section.groups
    (index-section items response)
    [:p
     [:a {:href "/groups/new"} "Create a new group"]]]})

(defview #'user-list :html
  [request user]
  {:body "user list"})

(defview #'new-page :html
  [request group]
  {:title "Create New Group"
   :body (add-form group)})

(defview #'add :html
  [request group]
  {:status 303
   :headers {"Location" "/groups"}
   :flash "Group added"
   :template false})

(defview #'edit-page :html
  [request group]
  {:title (:nickname group)
   :body
   [:div]})
