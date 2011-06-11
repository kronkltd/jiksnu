(ns jiksnu.helpers.auth-helpers
  (:use ciste.sections
        ciste.sections.default
        jiksnu.session)
  (:require [hiccup.form-helpers :as f]))

(defn login-uri
  []
  "/main/login")

(defn logout-uri
  []
  "/main/logout")

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

(defn logout-form
  []
  [:div
   (f/form-to
    [:post (logout-uri)]
    [:p (link-to (current-user))
     (f/submit-button "Logout")])])

(defn login-section
  []
  [:div#login-section
   (if-let [user (current-user)]
     (logout-form)
     [:a {:href "/main/login"} "Log in"])])
