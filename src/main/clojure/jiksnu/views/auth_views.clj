(ns jiksnu.views.auth-views
  (:use ciste.core
        ciste.html
        ciste.sections
        ciste.view
        jiksnu.http.controller.auth-controller
        jiksnu.http.view
        jiksnu.model
        jiksnu.view)
  (:require [hiccup.form-helpers :as f])
  (:import jiksnu.model.User))

(defn login-form
  []
  [:div
   [:div
    (f/form-to
     [:post (login-uri)]
     [:fieldset
      [:legend "Login"]
      [:ul
       [:li
        (f/label :username "Username")
        (f/text-field :username)]
       [:li
        (f/label :password "Password")
        (f/password-field  :password)]
       [:li (f/submit-button "Login")]]])]
   [:div
    (f/form-to
     [:post "/main/guest-login"]
     [:fieldset
      [:legend "Guest Login"]
      [:ul
       [:li
        (f/label :webid "Web Id:")
        (f/text-field :webid)]
       [:li (f/submit-button "Login")]]])]])

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
  [request id]
  {:session {:id id}
   :status 303
   :template false
   :headers {"Location" "/"}})

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
