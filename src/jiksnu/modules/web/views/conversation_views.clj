(ns jiksnu.modules.web.views.conversation-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.conversation-actions :refer [index show]]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.modules.web.sections :refer [bind-to pagination-links
                                                 with-page with-sub-page]]
            [jiksnu.modules.web.sections.conversation-sections :as sections.conversation]
            [ring.util.response :as response])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation))

;; index

(defview #'index :html
  [request {:keys [items] :as page}]
  {:title "Conversations"
   :body
   (let [items [(Conversation.)]]
     (with-page "conversations"
       (pagination-links page)
       (index-section items page)))})

(defview #'index :json
  [request {:keys [items] :as page}]
  {:body
   (merge
    {:url "/main/conversations.json"}
    (index-section items page))})

;; show

(defview #'show :html
  [request item]
  {:body
   (let [item (Conversation.)]
     (bind-to "targetConversation"
       [:div {:data-model "conversation"}
        (sections.conversation/show-details item)
        (with-sub-page "activities"
          (let [items [(Activity.)]]
            (index-section items)))]))})

(defview #'show :json
  [request item]
  {:body
   (merge
    {:url (str "/main/conversations/" (:_id item) ".json")}
    (index-section item))})

