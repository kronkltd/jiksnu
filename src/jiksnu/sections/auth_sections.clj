(ns jiksnu.sections.auth-sections
  (:use (ciste [debug :only [spy]])
        (jiksnu [session :only [current-user]]
                [views :only [control-line]])
        (jiksnu.sections [user-sections :only [display-avatar-img]])))

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
  [:form {:method "post" :action "/main/login"}
   [:fieldset
    [:legend "Enter Password"]
    [:input {:type "hidden" :name "username" :value (:username user)}]
    (control-line "Password" "password" "password")
    [:div.actions
     [:input {:type "submit" :value "Login"}]]]])
