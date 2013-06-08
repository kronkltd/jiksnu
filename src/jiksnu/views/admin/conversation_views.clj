(ns jiksnu.views.admin.conversation-views
  (:use [ciste.sections.default :only [show-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.conversation-actions :only [index show]]
        [jiksnu.sections :only [admin-index-section format-page-info pagination-links with-page]])
  (:require [jiksnu.model.conversation :as model.conversation]))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Conversations"
   :single true
   :body
   (with-page "conversations"
     (list (pagination-links response)
           (admin-index-section items response)))})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Conversations"}})

(defview #'show :html
  [request conversation]
  {:title "Conversation"
   :body (show-section conversation)})
