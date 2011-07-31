(ns jiksnu.views.auth-views
  (:use ciste.core
        ciste.debug
        ciste.html
        ciste.sections
        ciste.sections.default
        ciste.views
        jiksnu.actions.auth-actions
        jiksnu.helpers.auth-helpers
        jiksnu.model
        jiksnu.view)
  (:require [hiccup.form-helpers :as f])
  (:import jiksnu.model.User))

(defview #'password-page :html
  [request user]
  {:body
   [:div
    (f/form-to
     [:post (login-uri)]
     [:fieldset
      [:legend "Enter Password"]
      [:ul
       [:li.hidden (f/hidden-field :username (:username user))]
       [:li (f/label :password "Password")
        (f/password-field  :password)]
       [:li (f/submit-button "Login")]]])]})

(defview #'login-page :html
  [request _]
  {:title "Login"
   :body (login-form)})

(defview #'login :html
  [request user]
  (if user
    {:session {:id (:_id user)}
     :status 303
     :template false
     :headers {"Location" "/"}}))

(defview #'logout :html
  [request successful]
  (if successful
    {:session {:id nil}
     :status 303
     :template false
     :headers {"Location" "/"}}))

(defview #'guest-login :html
  [request user]
  {:status 303
   :template false
   :session {:pending-id (:_id user)}
   :headers {"Location" "/main/password"}})
