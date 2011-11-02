(ns jiksnu.views.auth-views
  (:use (ciste core
               [debug :only (spy)]
               sections views)
        ciste.sections.default
        (jiksnu model view)
        jiksnu.actions.auth-actions)
  (:require (jiksnu.templates [auth :as templates.auth])
            (ring.util [response :as response]))
  (:import jiksnu.model.User))

(defview #'guest-login :html
  [request user]
  {:status 303
   :template false
   :session {:pending-id (:_id user)}
   :headers {"Location" "/main/password"}})

(defview #'login :html
  [request user]
  (if user
    {:session {:id (:_id user)}
     :status 303
     :template false
     :headers {"Location" "/"}}))

(defview #'login-page :html
  [request _]
  {:title "Login"
   :body (templates.auth/login-form)})

(defview #'logout :html
  [request successful]
  (if successful
    {:session {:id nil}
     :status 303
     :template false
     :headers {"Location" "/"}}))

(defview #'password-page :html
  [request user]
  {:body (templates.auth/password-page user)})
