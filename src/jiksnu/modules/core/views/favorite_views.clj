(ns jiksnu.modules.core.views.favorite-views
  (:use [ciste.views :only [defview]]
        jiksnu.actions.favorite-actions))

(defview #'user-list :html
  [request user]
  {:body "user list"})
