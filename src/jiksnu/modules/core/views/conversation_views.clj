(ns jiksnu.modules.core.views.conversation-views
  (:require [ciste.views :refer [defview]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.conversation-actions :as actions.conversation])
  (:import jiksnu.model.Conversation))

;; index

(defview #'actions.conversation/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

(defview #'actions.conversation/index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Conversations"}})

;; show

(defview #'actions.conversation/show :model
  [request item]
  {:body item})

(defview #'actions.conversation/show :viewmodel
  [request item]
  {:body {:targetConversation (:_id item)
          :title (or (:title item)
                     "Conversation")}})
