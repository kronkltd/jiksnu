(ns jiksnu.views.admin.conversation-views
  (:use [ciste.sections.default :only [show-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.conversation-actions :only [index show]]
        [jiksnu.sections.conversation-sections :only [admin-index-section]])
  (:require [jiksnu.model.conversation :as model.conversation]))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Conversations"
   :single true
   :body (admin-index-section items response)})

(defview #'show :html
  [request conversation]
  {:title "Conversation"
   :body (show-section conversation)})
