(ns jiksnu.http.view.auth-view
  (:use ciste.core
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
       (f/password-field  :password)]]
     (f/submit-button "Login")])])

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
