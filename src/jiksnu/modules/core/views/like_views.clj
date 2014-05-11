(ns jiksnu.modules.core.views.like-views
  (:require [ciste.views :refer [defview]]
            [jiksnu.actions.like-actions :as actions.like]))

(defview #'actions.like/like-activity :html
  [request like]
  {:status 303
   ;; TODO: redirect to redirect url
   :headers {"Location" "/"}
   :template false})

(defview #'actions.like/delete :html
  [request _]
  {:status 303
   :flash "like deleted"
   :template false
   :headers {"Location" "/"}})
