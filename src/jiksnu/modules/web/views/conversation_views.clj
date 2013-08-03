(ns jiksnu.modules.web.views.conversation-views
  (:use [ciste.core :only [with-format]]
        [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section]]
        [clojurewerkz.route-one.core :only [named-path]]
        jiksnu.actions.conversation-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [bind-to dump-data format-page-info
                                pagination-links with-page with-sub-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.modules.web.sections.conversation-sections :as sections.conversation]
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
