(ns jiksnu.views.like-views
  (:use [ciste.views :only [defview]]
        [clojurewerkz.route-one.core :only [named-path]]
        jiksnu.actions.like-actions))

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
