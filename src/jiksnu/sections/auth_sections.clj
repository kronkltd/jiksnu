(ns jiksnu.sections.auth-sections
  (:use (ciste [debug :only [spy]]
               [sections :only [defsection]])
        ciste.sections.default
        (jiksnu [session :only [current-user]]
                [views :only [control-line]])
        (jiksnu.sections [user-sections :only [display-avatar-img]]))
  (:import jiksnu.model.AuthenticationMechanism))

(defn logout-button
  [user]
  [:li.dropdown
   [:a.dropdown-toggle {:href "#" :data-toggle "dropdown"}
    (display-avatar-img user 18)
    (:display-name user) [:b.caret]]
   [:ul.dropdown-menu
    [:li
     [:a.logout-link {:href #_"#" "/main/logout"} "Logout"]]]])

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
    [:legend"Add a mechanism"
     (control-line "Type" "type" "text")
     (control-line "Value" "value" "text")
     [:div.actions
      [:input.btn.btn-primary {:type "submit" :value "Add"}]]]]])
