(ns jiksnu.views.admin.conversation-views
  (:use [ciste.debug :only [spy]]
        [ciste.sections.default :only [show-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.conversation-actions :only [index show]]
        [jiksnu.sections.conversation-sections :only [admin-index-section]])
  (:require [jiksnu.model.conversation :as model.conversation]))

(defview #'index :html
  [request [conversations options]]
  {:title "Conversations"
   :single true
   :body (admin-index-section conversations)})

(defview #'show :html
  [request conversation]
  {:title "Conversation"
   :body (show-section conversation)})
