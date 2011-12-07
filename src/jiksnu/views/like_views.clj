(ns jiksnu.views.like-views
  (:use (ciste [views :only [defview]])
        jiksnu.actions.like-actions))

(defview #'like-activity :html
  [request like]
  {:status 303
   :headers {"Location" "/"}
   :template false})
