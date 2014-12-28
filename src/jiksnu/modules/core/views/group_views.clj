(ns jiksnu.modules.core.views.group-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section add-form show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.group-actions :as actions.group :refer [add create edit-page index
                                                  new-page show user-list]]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.modules.web.sections :refer [format-page-info pagination-links with-page]])
  (:import jiksnu.model.Group))

(defview #'actions.group/fetch-admins :page
  [request {:keys [items] :as page}]
  (let [response (merge page
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "sub-page-updated"
            :model "group"
            :id (:_id (:item request))
            :body response}}))

(defview #'actions.group/fetch-by-user :page
  [request page]
  (let [items (:items page)
        response (merge page
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "sub-page-updated"
            :model "user"
            :id (:_id (:item request))
            :body response}}))

(defview #'actions.group/fetch-by-user :viewmodel
  [request page]
  {:body {:title "Groups"
          :pages {:groups (format-page-info page)}}})

;; index

(defview #'actions.group/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

(defview #'actions.group/index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Groups"
          :pages {:groups (format-page-info page)}
          :groups (index-section items page)}})

;; new-page

(defview #'actions.group/new-page :viewmodel
  [request group]
  {:body {:title "Create New Group"}})

;; show

(defview #'actions.group/show :model
  [request item]
  {:body (doall (show-section item))})

(defview #'actions.group/show :viewmodel
  [request item]
  (let [id (:_id item)]
    {:body
     {:title (:nickname item)
      :pages {:activities (let [page (actions.activity/index {:group id})]
                            (format-page-info page))}
      :targetGroup (:_id item)}}))
