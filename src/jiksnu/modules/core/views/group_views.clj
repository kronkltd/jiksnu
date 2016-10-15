(ns jiksnu.modules.core.views.group-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section]]
            [jiksnu.actions.group-actions :as actions.group]))

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
  {:body (merge page
                {:title "Groups"
                 :model "user"
                 :id    (:_id (:item page))})})

(defview #'actions.group/index :page
  [request page]
  {:body (merge page
                {:id    (:name request)
                 :title "Groups"})})
