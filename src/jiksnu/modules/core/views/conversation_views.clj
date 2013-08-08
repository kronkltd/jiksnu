(ns jiksnu.modules.core.views.conversation-views
  (:use [ciste.views :only [defview]]
        jiksnu.actions.conversation-actions)
  (:require [clojure.tools.logging :as log])
  (:import jiksnu.model.Conversation))

;; index

(defview #'index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Conversations"}})

;; show

(defview #'show :model
  [request item]
  {:body item})

(defview #'show :viewmodel
  [request item]
  {:body {:targetConversation (:_id item)
          :title (or (:title item)
                     "Conversation")}})
