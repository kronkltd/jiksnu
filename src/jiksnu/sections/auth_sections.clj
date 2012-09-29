(ns jiksnu.sections.auth-sections
  (:use [ciste.sections :only [defsection]]
        ciste.sections.default
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [control-line]]
        [jiksnu.session :only [current-user]]
        [jiksnu.sections.user-sections :only [display-avatar-img]])
  (:require [clojure.tools.logging :as log])
  (:import jiksnu.model.AuthenticationMechanism))

(defn logout-button
  [user]
  [:li.dropdown
   [:a.dropdown-toggle (merge {:href "#" :data-toggle "dropdown"})
    [:span
     (if *dynamic* {:data-bind "with: currentUser"})
     [:span {:data-model "user"}
      (display-avatar-img user 18)
      [:span
       (if *dynamic*
         {:data-bind "text: displayName"}
         (:display-name user))]]] [:b.caret]]
   [:ul.dropdown-menu
    [:li
     [:a.settings-link {:href "/main/settings"} "Settings"]
     [:a.profile-link {:href "/main/profile"} "Profile"]
     ;; TODO: need a better way to do this when there is no javascript
     [:a.logout-link {:href "/main/logout?_method=POST"} "Log out"]]]])

(defn login-button
  []
  (list
   [:li.unauthenticated [:a.login-link {:href "/main/login"} "Login"]]
   [:li.divider-vertical]
   [:li [:a.register-link {:href "/main/register"} "Register"]]))

(defn login-section
  [response]
  (if-let [authenticated (current-user)]
    (logout-button authenticated)
    (login-button)))

(defn password-page
  [user]
  [:form.well {:method "post" :action "/main/login"}
   [:fieldset
    [:legend "Enter Password"]
    [:input {:type "hidden" :name "username" :value (:username user)}]
    (control-line "Password" "password" "password")
    [:div.actions
     [:input.btn.btn-primary {:type "submit" :value "Login"}]]]])

(defsection add-form [AuthenticationMechanism :html]
  [mechanism & _]
  [:form.well {:method "post" :action "/admin/auth"}
   [:fieldset
    [:legend "Add a mechanism"
     (control-line "Type" "type" "text")
     (control-line "Value" "value" "text")
     [:div.actions
      [:input.btn.btn-primary {:type "submit" :value "Add"}]]]]])
