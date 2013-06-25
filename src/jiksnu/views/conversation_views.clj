(ns jiksnu.views.conversation-views
  (:use [ciste.core :only [with-format]]
        [ciste.views :only [defview]]
        [ciste.sections.default :only [uri index-section show-section]]
        [clojurewerkz.route-one.core :only [named-path]]
        jiksnu.actions.conversation-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [bind-to dump-data format-page-info
                                pagination-links with-page with-sub-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.sections.conversation-sections :as sections.conversation]
            [ring.util.response :as response])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation))

;; index

(defview #'index :html
  [request {:keys [items] :as page}]
  {:title "Conversations"
   :body
   (let [items (if *dynamic*
                 [(Conversation.)]
                 items)]
     (with-page "conversations"
       (pagination-links page)
       (doall (index-section items page))))})

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

(defview #'show :html
  [request item]
  {:body
   (let [item (if *dynamic* (Conversation.) item)]
     (bind-to "targetConversation"
       [:div {:data-model "conversation"}
        (sections.conversation/show-details item)
        (with-sub-page "activities"
          (let [items (if *dynamic*
                        [(Activity.)]
                        (:items (actions.activity/fetch-by-conversation item)))]
            (index-section items)))]))})

(defview #'show :model
  [request item]
  {:body item})

(defview #'show :viewmodel
  [request item]
  {:body {:targetConversation (:_id item)
          :title (or (:title item)
                     "Conversation")}})
