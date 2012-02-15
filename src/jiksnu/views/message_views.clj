(ns jiksnu.views.message-views
  (:use (ciste [views :only [defview]])
        jiksnu.actions.message-actions))

(defview #'inbox-page :html
  [request messages]
  {:title "Inbox"})

(defview #'outbox-page :html
  [request messages]
  {:title "Oubox"})
