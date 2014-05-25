(ns jiksnu.modules.web.views.group-views
  (:require [ciste.model :as cm]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section add-form
                                            show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.modules.web.sections :refer [bind-to format-page-info
                                                 pagination-links with-page
                                                 with-sub-page]]
            [jiksnu.modules.web.sections.group-sections :as sections.group])
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
  {:body
   (let [items (if *dynamic* [(Group.)] items)]
     (with-page "groups"
       (pagination-links response)
       (index-section items response)))})

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

(defview #'actions.group/join :html
  [request group]
  {:status 303
   :headers {"Location" "/main/groups"}
   :flash "User joined"
   :template false})

;; new-page

(defview #'actions.group/new-page :html
  [request group]
  {:title "Create New Group"
   :body (add-form group)})

(defn conversation-sub-page
  [group]
  (let [{:keys [items] :as page} (if *dynamic*
                                   {:items []}
                                   (cm/implement {:items []}))]
    (with-sub-page "conversations"
      (pagination-links page)
      (index-section items page))))

(defview #'actions.group/show :html
  [request group]
  {:title (str (:nickname group) " group")
   :post-form true
   :body
   (bind-to "targetGroup"
     (show-section group)
     [:div {:data-model "group"}
      [:div
       [:h3 "actions"]
       (sections.group/join-button group)
       (sections.group/leave-button group)]
      (conversation-sub-page group)])})

;; show

(defview #'actions.group/user-list :html
  [request user]
  {:body "user list"})
