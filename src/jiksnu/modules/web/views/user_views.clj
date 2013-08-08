(ns jiksnu.modules.web.views.user-views
  (:use [ciste.core :only [with-format]]
        [ciste.views :only [defview]]
        [ciste.sections.default :only [uri index-section show-section]]
        [clojurewerkz.route-one.core :only [add-route! named-path]]
        jiksnu.actions.user-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [bind-to pagination-links with-page]])
  (:require [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.namespace :as ns]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.web.sections.user-sections :as sections.user]
            [ring.util.response :as response]))

;; create

(defview #'create :html
  [request user]
  (-> (response/redirect-after-post (uri user))
      (assoc :template false)
      (assoc :flash "user has been created")))

;; delete

(defview #'delete :html
  [request _]
  (-> (named-path "public timeline")
      response/redirect-after-post
      (assoc :template false)
      (assoc :flash "user has been deleted")))

;; discover

(defview #'discover :html
  [request user]
  (-> (named-path "index users")
      response/redirect-after-post
      (assoc :template false)
      (assoc :flash "discovering user")))

;; fetch-updates

;; (defview #'fetch-updates :html
;;   [request user]
;;   (-> (response/redirect-after-post (uri user))
;;       (assoc :template false)
;;       (assoc :flash "fetching updates")))

;; index

(defview #'index :html
  [request {:keys [items] :as page}]
  {:title "Users"
   :body (with-page "users"
           (pagination-links page)
           (index-section items page))})

;; profile

(defview #'profile :html
  [request user]
  {:title "Edit Profile"
   :body [:div (sections.user/edit-form user)]})

;; register

(defview #'register :html
  [request user]
  (-> (named-path "public timeline")
      response/redirect-after-post
      (assoc :template false)
      (assoc :flash "user has been created")
      (assoc :session {:id (:_id user)})))

;; register-page

(defview #'register-page :html
  [request user]
  {:title "Register"
   :body (sections.user/register-form user)})

;; show

(defview #'show :html
  [request user]
  {:template false
   :body (with-format :as
           (show-section user))})

;; update

(defview #'update :html
  [request user]
  {:status 302
   :template false
   :flash "User updated"
   :headers {"Location" (uri user)}})

;; update-profile

(defview #'update-profile :html
  [request user]
  {:status 303
   :template false
   :flash "Profile updated"
   :headers {"Location" "/main/profile"}})

;; user-meta

(defview #'user-meta :html
  [request user]
  {:template false
   :headers {"Content-Type" "application/xrds+xml"
             "Access-Control-Allow-Origin" "*"}
   :body (h/html (model.webfinger/user-meta user))})

