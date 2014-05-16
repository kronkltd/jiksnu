(ns jiksnu.modules.web.views.group-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section add-form show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.group-actions :as actions.group :refer [add create edit-page index
                                                  new-page show user-list]]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.modules.web.sections :refer [format-page-info pagination-links with-page]])
  (:import jiksnu.model.Group))

(defview #'actions.group/add :html
  [request group]
  {:status 303
   :headers {"Location" "/main/groups"}
   :flash "Group added"
   :template false})

(defview #'actions.group/create :html
  [request group]
  {:status 303
   :headers {"Location" "/main/groups"}
   :flash "Group added"
   :template false})

(defview #'actions.group/edit-page :html
  [request group]
  {:title (:nickname group)
   :body
   [:div]})

(defview #'actions.group/fetch-by-user :html
  [request {:keys [items] :as response}]
  {:body (with-page "groups"
     (let [items (if *dynamic* [(Group.)] items)]
       (list
        (pagination-links response)
        (index-section items response))))})

;; index

(defview #'actions.group/index :html
  [request {:keys [items] :as response}]
  {:title "Groups"
   :body
   (with-page "groups"
     (let [items (if *dynamic* [(Group.)] items)]
       (list
        (pagination-links response)
        (index-section items response)
        [:p
         [:a {:href "/main/groups/new"}
          "Create a new group"]])))})

;; new-page

(defview #'actions.group/new-page :html
  [request group]
  {:title "Create New Group"
   :body (add-form group)})

;; show

(defview #'actions.group/user-list :html
  [request user]
  {:body "user list"})
