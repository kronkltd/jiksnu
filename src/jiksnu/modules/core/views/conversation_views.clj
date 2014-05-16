(ns jiksnu.modules.core.views.conversation-views
  (:require [ciste.views :refer [defview]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.conversation-actions :as actions.conversation])
  (:import jiksnu.model.Conversation))

(defview #'actions.conversation/index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Conversations"}})

(defview #'actions.conversation/show :viewmodel
  [request item]
  {:body {:targetConversation (:_id item)
          :title (or (:title item)
                     "Conversation")}})


(defview #'actions.conversation/fetch-by-group :page
  [request {:keys [items] :as page}]
  (let [response (merge page
                        {:id (:name (log/spy :info request))
                         :items (map :_id items)})]
    {:body {:action "sub-page-updated"
            :model "group"
            :id (:_id (:item page))
            :body response}}))

(defview #'actions.conversation/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))



(defview #'actions.conversation/show :model
  [request item]
  {:body item})

