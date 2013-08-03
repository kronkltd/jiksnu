(ns jiksnu.modules.web.views.activity-views
  (:use [ciste.config :only [config]]
        [ciste.views :only [defview]]
        ciste.sections.default
        [clojurewerkz.route-one.core :only [named-path]]
        jiksnu.actions.activity-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [bind-to format-page-info]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.web.sections.activity-sections :as sections.activity]
            [jiksnu.session :as session]
            [jiksnu.modules.xmpp.element :as xmpp.element]
            [ring.util.response :as response])
  (:import jiksnu.model.Activity))

;; delete

(defview #'delete :html
  [request activity]
  (-> (named-path "public timeline")
      response/redirect-after-post
      (assoc :template false)))

;; edit

(defview #'edit :html
  [request activity]
  (let [actor (session/current-user)]
    (-> (response/redirect-after-post (uri actor))
        (assoc :template false))))

;; edit-page

;; (defview #'edit-page :html
;;   [request activity]
;;   {:body (edit-form activity)})

;; post

(defview #'post :html
  [request activity]
  (let [actor (session/current-user)
        url (or (-> request :params :redirect_to)
                (named-path "public timeline")
                (uri actor))]
    (-> (response/redirect-after-post url)
        (assoc :template false))))

;; show

(defview #'show :html
  [request activity]
  {:body
   (let [activity (if *dynamic* (Activity.) activity)]
     (bind-to "targetActivity"
       (show-section activity)))})

