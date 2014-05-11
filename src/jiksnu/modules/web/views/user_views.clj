(ns jiksnu.modules.web.views.user-views
  (:require [ciste.core :refer [with-format]]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [uri index-section show-section]]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.namespace :as ns]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.web.sections.user-sections :as sections.user]
            [jiksnu.modules.web.sections :refer [bind-to pagination-links with-page]]
            [jiksnu.routes.helpers :refer [add-route! named-path]]
            [ring.util.response :as response]))

(defview #'actions.user/add-stream :html
  [request [user stream]]
  (-> (response/redirect-after-post (uri user))
      (assoc :template false)
      (assoc :flash "stream has been created")))

;; create

(defview #'actions.user/create :html
  [request user]
  (-> (response/redirect-after-post (uri user))
      (assoc :template false)
      (assoc :flash "user has been created")))

;; delete

(defview #'actions.user/delete :html
  [request _]
  (-> "/"
      response/redirect-after-post
      (assoc :template false)
      (assoc :flash "user has been deleted")))

;; discover

(defview #'actions.user/discover :html
  [request user]
  (-> "/users"
      response/redirect-after-post
      (assoc :template false)
      (assoc :flash "discovering user")))

;; fetch-updates

;; (defview #'actions.user/fetch-updates :html
;;   [request user]
;;   (-> (response/redirect-after-post (uri user))
;;       (assoc :template false)
;;       (assoc :flash "fetching updates")))

;; index

(defview #'actions.user/index :html
  [request {:keys [items] :as page}]
  {:title "Users"
   :body (with-page "users"
           (pagination-links page)
           (index-section items page))})

;; profile

(defview #'actions.user/profile :html
  [request user]
  {:title "Edit Profile"
   :body [:div (sections.user/edit-form user)]})

;; register

(defview #'actions.user/register :html
  [request user]
  (-> "/"
      response/redirect-after-post
      (assoc :template false)
      (assoc :flash "user has been created")
      (assoc :session {:id (:_id user)})))

;; register-page

(defview #'actions.user/register-page :html
  [request user]
  {:title "Register"
   :body (sections.user/register-form user)})

;; show

(defview #'actions.user/show :html
  [request user]
  {:template false
   :body (with-format :as
           (show-section user))})

;; update

(defview #'actions.user/update :html
  [request user]
  {:status 302
   :template false
   :flash "User updated"
   :headers {"Location" (uri user)}})

;; update-profile

(defview #'actions.user/update-profile :html
  [request user]
  {:status 303
   :template false
   :flash "Profile updated"
   :headers {"Location" "/main/profile"}})

;; user-meta

(defview #'actions.user/user-meta :html
  [request user]
  {:template false
   :headers {"Content-Type" "application/xrds+xml"
             "Access-Control-Allow-Origin" "*"}
   :body (h/html (model.webfinger/user-meta user))})

