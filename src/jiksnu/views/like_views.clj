(ns jiksnu.views.like-views
  (:use [ciste.views :only [defview]]
        jiksnu.actions.like-actions))

(defview #'like-activity :html
  [_request _like]
  {:status 303
   ;; TODO: redirect to redirect url
   :headers {"Location" "/"}
   :template false})

(defview #'delete :html
  [request _]
  {:status 303
   :flash "like deleted"
   :template false
   :headers {"Location" "/"}})
