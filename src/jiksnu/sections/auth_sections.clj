(ns jiksnu.sections.auth-sections
  (:use (ciste [debug :only [spy]])
        (jiksnu [session :only [current-user]])))

(defn logout-button
  [user]
  [:ul.nav.secondary-nav
   [:li.dropdown
    [:a.dropdown-toggle {:href "#"} (:display-name (spy user))]
    [:ul.dropdown-menu
     [:li
      [:a {:href "/main/logout"} "Logout"]]]]])

(defn login-button
  []
  [:span
   [:a {:href "/main/login"} "Login"]
   " or "
   [:a {:href "/main/register"} "Register"]])

(defn login-section
  [response]
  (let [authenticated (current-user)]
    [:section#session-menu.pull-right
     [:div#login-section
      {:class (if authenticated "authenticated" "unauthenticated")}
      (if authenticated
        (logout-button authenticated)
        (login-button))]]))

(defn password-page
  [user]
  [:form {:method "post" :action "/main/login"}
   [:fieldset
    [:legend "Enter PAssword"]
    [:input {:type "hidden" :name "username" :value (:username user)}]
    [:div.clearfix
     [:label {:for "password"} "Password"]
     [:div.input
      [:input {:type "password" :name "password"}]]]
    [:div.actions
     [:input {:type "submit" :value "Login"}]]]])
