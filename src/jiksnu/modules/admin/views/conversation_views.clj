(ns jiksnu.modules.admin.views.conversation-views
  (:use [ciste.sections.default :only [show-section]]
        [ciste.views :only [defview]]
        [jiksnu.modules.admin.actions.conversation-actions :only [index show]]
        [jiksnu.modules.core.sections :only [admin-index-section]]
        [jiksnu.modules.web.sections :only [pagination-links with-page]])
  (:import jiksnu.model.Conversation))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:title "Conversations"
   :single true
   :body
   (let [items [(Conversation.)]]
     (with-page "conversations"
       (pagination-links response)
       (doall (admin-index-section items response))))})

(defview #'index :json
  [request {:keys [items] :as page}]
  {:body
   (merge
    {:url "/admin/conversations.json"}
    {:items (admin-index-section items page)})})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Conversations"}})

(defview #'show :html
  [request conversation]
  {:title "Conversation"
   :body (show-section conversation)})
