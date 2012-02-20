(ns jiksnu.sections.auth-sections
  (:use (ciste [debug :only [spy]])
        (jiksnu [session :only [current-user]])
        (jiksnu.sections [user-sections :only [display-avatar-img]])))

(defn logout-button
  [user]
  [:li.dropdown
   [:a.dropdown-toggle {:href "#" :data-toggle "dropdown"}
    #_(display-avatar-img user 18)
    (:display-name user) [:b.caret]]
   [:ul.dropdown-menu
    [:li
     [:a {:href "#" #_"/main/logout"} "Logout"]]]])

(defn login-button
  []
  (list
   [:li [:a {:href "/main/login"} "Login"]]
   [:li.divider-vertical]
   [:li [:a {:href "/main/register"} "Register"]]))

(defn login-section
  [response]
  (if-let [authenticated (current-user)]
    (logout-button authenticated)
    (login-button)))

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
