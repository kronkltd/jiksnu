(ns jiksnu.modules.core.views.like-views
  (:require [ciste.views :refer [defview]]
            jiksnu.actions.like-actions
            [jiksnu.routes.helpers :refer [named-path]]))

(defview #'like-activity :html
  [request like]
  {:status 303
   ;; TODO: redirect to redirect url
   :headers {"Location"  (named-path "public timeline")}
   :template false})

(defview #'delete :html
  [request _]
  {:status 303
   :flash "like deleted"
   :template false
   :headers {"Location" (named-path "public timeline")}})
