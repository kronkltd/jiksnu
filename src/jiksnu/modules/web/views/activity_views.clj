(ns jiksnu.modules.web.views.activity-views
  (:require [ciste.config :refer [config]]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section uri]]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.web.sections.activity-sections :as sections.activity]
            [jiksnu.routes.helpers :refer [named-path]]
            [jiksnu.session :as session]
            [jiksnu.modules.web.sections :refer [bind-to]]
            [ring.util.response :as response])
  (:import jiksnu.model.Activity))

;; delete

(defview #'actions.activity/delete :html
  [request activity]
  (-> "/"
      response/redirect-after-post
      (assoc :template false)))

;; edit

(defview #'actions.activity/edit :html
  [request activity]
  (let [actor (session/current-user)]
    (-> (response/redirect-after-post (uri actor))
        (assoc :template false))))

;; edit-page

;; (defview #'actions.activity/edit-page :html
;;   [request activity]
;;   {:body (edit-form activity)})

;; post

(defview #'actions.activity/post :html
  [request activity]
  (let [actor (session/current-user)
        url (or (-> request :params :redirect_to)
                "/"
                (uri actor))]
    (-> (response/redirect-after-post url)
        (assoc :template false))))

;; show

(defview #'actions.activity/show :html
  [request activity]
  {:body
   (let [activity (if *dynamic* (Activity.) activity)]
     (bind-to "targetActivity"
       (show-section activity)))})

