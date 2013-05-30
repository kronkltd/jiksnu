(ns jiksnu.views.group-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section add-form show-section]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.actions.group-actions :only [add create edit-page index
                                             new-page show user-list]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [format-page-info pagination-links with-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity])
  (:import jiksnu.model.Group))

(defview #'add :html
  [request group]
  {:status 303
   :headers {"Location" (named-path "index groups")}
   :flash "Group added"
   :template false})

(defview #'create :html
  [request group]
  {:status 303
   :headers {"Location" (named-path "index groups")}
   :flash "Group added"
   :template false})

(defview #'edit-page :html
  [request group]
  {:title (:nickname group)
   :body
   [:div]})

(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Groups"
   :body
   (with-page "groups"
     (let [items (if *dynamic* [(Group.)] items)]
       (list
        (pagination-links response)
        (index-section items response)
        [:p
         [:a {:href (named-path "new group")}
          "Create a new group"]])))})

(defview #'index :json
  [request {:keys [items] :as page}]
  {:body
   {:items (index-section items page)}})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Groups"
          :pages {:groups (format-page-info page)}
          :groups (index-section items page)}})

(defview #'new-page :html
  [request group]
  {:title "Create New Group"
   :body (add-form group)})

;; show

(defview #'show :model
  [request item]
  {:body (doall (show-section item))})

(defview #'show :viewmodel
  [request item]
  (let [id (:_id item)]
    {:body
     {:title (:nickname item)
      :pages {:activities (let [page (actions.activity/index {:group id})]
                            (format-page-info page))}
      :targetGroup (:_id item)}}))



(defview #'user-list :html
  [request user]
  {:body "user list"})
