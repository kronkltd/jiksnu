(ns jiksnu.views.group-views
  (:use (ciste [views :only [defview]])
        jiksnu.actions.group-actions)
  )

(defview #'user-list :html
  [request user]
  {:body "user list"}
  )
