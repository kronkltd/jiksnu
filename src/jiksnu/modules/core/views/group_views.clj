(ns jiksnu.modules.core.views.group-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section show-section]]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.modules.core.sections :refer [format-page-info]]))

(defview #'actions.group/fetch-admins :page
  [request {:keys [items] :as page}]
  (let [response (merge page
                        {:id (:name request)})]
    {:body {:action "sub-page-updated"
            :model "group"
            :id (:_id (:item request))
            :body response}}))

(defview #'actions.group/fetch-by-user :page
  [request page]
  (let [items (:items page)
        response (merge page
                        {:id (:name request)})]
    {:body {:action "sub-page-updated"
            :title "Groups"
            :model "user"
            :id (:_id (:item request))
            :body response}}))

;; index

(defview #'actions.group/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)})]
    {:body {:action "page-updated"
            :title "Groups"
            :body response}}))
