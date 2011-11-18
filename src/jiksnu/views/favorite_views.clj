(ns jiksnu.views.favorite-views
  (:use (ciste [config :only [definitializer]]
               [views :only [defview]])
        jiksnu.actions.favorite-actions))

(defview #'user-list :html
  [request user]
  {:body "user list"})
