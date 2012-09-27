(ns jiksnu.views.admin.conversation-views
  (:use [ciste.sections.default :only [show-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.conversation-actions :only [index show]]
        [jiksnu.sections :only [admin-index-section format-page-info]])
  (:require [jiksnu.model.conversation :as model.conversation]))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Conversations"
   :single true
   :body (admin-index-section items response)})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Conversations"
          :pageInfo (format-page-info page)
          :items (map :_id items)
          :users (admin-index-section items page)}})





(defview #'show :html
  [request conversation]
  {:title "Conversation"
   :body (show-section conversation)})
