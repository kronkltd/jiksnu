(ns jiksnu.views.auth-views
  (:use ciste.core
        [ciste.views :only [defview]]
        ciste.sections.default
        jiksnu.model
        jiksnu.actions.auth-actions)
  (:require [clojure.tools.logging :as log]
            [jiksnu.sections.auth-sections :as sections.auth]
            [ring.util.response :as response])
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

(defview #'login :text
  [request user]
  {:body (format "logged in as %s" (:username user))})


(defview #'login-page :html
  [request _]
  {:title "Login"
   :body
   [:div
    [:div
     [:form {:method "post" :action "/main/login"}
      [:fieldset
       [:legend "Login"]
       [:div.clearfix
        [:label {:for "username"} "Username"]
        [:div.input
         [:input {:type "text" :name "username"}]]]
       [:div.clearfix
        [:label {:for "password"} "Password"]
        [:div.input
         [:input {:type "password" :name "password"}]]]
       [:div.actions
        [:input.btn.primary {:type "submit" :value "Login"}]]]]]
    [:div
     [:form {:method "post" :action "/main/guest-login"}
      [:fieldset
       [:legend "Guest Login"]
       [:div.clearfix
        [:label {:for "webid"} "Web Id"]
        [:div.input
         [:input {:type "text" :name "webid"}]]]
       [:div.actions
        [:input.btn.primary {:type "submit" :value "Login"}]]]]]]})

(defview #'logout :html
  [request successful]
  (if successful
    {:session {:id nil}
     :status 303
     :template false
     :headers {"Location" "/"}}))

(defview #'password-page :html
  [request user]
  {:body (sections.auth/password-page user)})

(defview #'verify-credentials :json
  [request _]
  {:body {:error "Could not authenticate you"
          :request (:uri request)}
   :template false})

(defview #'whoami :text
  [request user]
  {:body user})
