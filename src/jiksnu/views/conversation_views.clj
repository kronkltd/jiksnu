(ns jiksnu.views.conversation-views
  (:use [ciste.core :only [with-format]]
        [ciste.views :only [defview]]
        [ciste.sections.default :only [uri index-section show-section]]
        [clojurewerkz.route-one.core :only [named-path]]
        jiksnu.actions.conversation-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [dump-data format-page-info pagination-links with-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
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
     (with-page "default"
       (list (pagination-links page)
             [:div (if *dynamic* {:data-bind "with: items"})
              (doall (index-section items page))])))})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Conversations"
          :pages {:default (format-page-info page)}
          }})


;; show

(defview #'show :html
  [request item]
  {:body
   [:div (if *dynamic*
           {:data-bind "with: targetConversation"})
    (let [item (if *dynamic*
                     (Conversation.)
                     item)]
      (list
       [:div {:data-model "conversation"}
       (show-section item)]
            (with-page "activities"
              (list #_(dump-data)
                    (pagination-links {})
                    [:div {:data-bind "with: items"}
                     (index-section [(Activity.)])]))))]})

(defview #'show :model
  [request item]
  {:body item})

(defview #'show :viewmodel
  [request item]
  {:body {:targetConversation (:_id item)
          :pages {:activities (let [page (actions.activity/fetch-by-conversation item)]
                                (format-page-info page))}
          :title (or (:title item)
                     "Conversation")}})

