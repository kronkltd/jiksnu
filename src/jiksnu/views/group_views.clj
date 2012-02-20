(ns jiksnu.views.group-views
  (:use (ciste [views :only [defview]])
        (ciste.sections [default :only [index-section add-form]])
        (jiksnu [views :only [control-line]])
        jiksnu.actions.group-actions))

(defview #'index :html
  [request groups]
  {:title "Groups"
   :body
   [:section.groups
    (index-section groups)
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
   :headers {"Location" "/group"}
   :template false})
