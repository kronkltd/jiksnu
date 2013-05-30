(ns jiksnu.views.conversation-views
  (:use [ciste.core :only [with-format]]
        [ciste.views :only [defview]]
        [ciste.sections.default :only [uri index-section show-section]]
        [clojurewerkz.route-one.core :only [named-path]]
        jiksnu.actions.conversation-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [bind-to dump-data format-page-info pagination-links with-page]])
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
       (bind-to "items"
         (doall (index-section items page)))))})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Conversations"
          :pages {:conversations (format-page-info page)}}})

;; show

(defview #'show :html
  [request item]
  {:body
   (let [item (if *dynamic* (Conversation.) item)]
     (bind-to "targetConversation"
       [:div {:data-model "conversation"}
        (sections.conversation/show-details item)
        (show-section item)]
       ))})

(defview #'show :model
  [request item]
  (let [page (actions.activity/fetch-by-conversation item
                                                     {:sort-clause {:updated 1}})
        page (assoc page :items (map :_id (:items page)))]
    {:body (assoc item :activities page)}))

(defview #'show :viewmodel
  [request item]
  {:body {:targetConversation (:_id item)
          :pages {:activities (let [page (actions.activity/fetch-by-conversation item
                                                                                 {:sort-clause {:updated 1}})]
                                (format-page-info page))}
          :title (or (:title item)
                     "Conversation")}})
