(ns jiksnu.modules.web.views.message-views
  (:use [ciste.views :only [defview]]
        [jiksnu.actions.message-actions :as actions.message]))

(defview #'actions.message/inbox-page :html
  [request messages]
  {:title "Inbox"})

(defview #'actions.message/outbox-page :html
  [request messages]
  {:title "Oubox"})
