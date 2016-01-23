(ns jiksnu.modules.core.views.conversation-views
  (:require [ciste.views :refer [defview]]
            [jiksnu.actions.conversation-actions :as actions.conversation]))

(defview #'actions.conversation/fetch-by-group :page
  [request {:keys [items] :as page}]
  (let [response (merge page
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "sub-page-updated"
            :model "group"
            :id (:_id (:item request))
            :body response}}))

(defview #'actions.conversation/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))
