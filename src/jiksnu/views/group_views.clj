(ns jiksnu.views.group-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section add-form]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.actions.group-actions :only [add create edit-page index
                                             new-page user-list]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [format-page-info pagination-links with-page]])
  (:require [clojure.tools.logging :as log])
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
   :viewmodel (str (named-path "index groups") ".viewmodel")
   :body
   (with-page "default"
     (list
      (pagination-links response)
      (index-section (if *dynamic*
                            [(Group.)]
                            items) response)
           [:p
            [:a {:href (named-path "new group")}
             "Create a new group"]]))})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Groups"
          :pages {:default (format-page-info page)}
          :groups (index-section items page)}})

(defview #'new-page :html
  [request group]
  {:title "Create New Group"
   :body (add-form group)})

(defview #'user-list :html
  [request user]
  {:body "user list"})
