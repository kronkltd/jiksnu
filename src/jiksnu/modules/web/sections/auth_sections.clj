(ns jiksnu.modules.web.sections.auth-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [actions-section add-form index-block
                                       index-line link-to]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.core.sections :only [admin-index-block admin-index-line]]
        [jiksnu.modules.web.sections :only [bind-to control-line display-property
                                            dump-data]]
        [jiksnu.session :only [current-user]]
        [jiksnu.modules.web.sections.user-sections :only [display-avatar-img]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.AuthenticationMechanism
           jiksnu.model.User))

(defn logout-button
  [user]
  [:li.dropdown
   [:a.dropdown-toggle (merge {:href "#" :data-toggle "dropdown"})
    [:span
     (if *dynamic* {:data-bind "with: currentUser"})
     [:span {:data-model "user"}
      (display-avatar-img user 18)
      (display-property user :name)]]
    [:b.caret]]
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

(defn ng-login-section
  [response]
  (if-let [authenticated (current-user)]
    (logout-button authenticated)
    (login-button)))

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

;; actions-section

(defsection actions-section [AuthenticationMechanism :html]
  [mechanism & _]
  [:ul])

;; add-form

(defsection add-form [AuthenticationMechanism :html]
  [mechanism & _]
  [:form.well {:method "post" :action "/admin/auth"}
   [:fieldset
    [:legend "Add a mechanism"
     (control-line "Type" "type" "text")
     (control-line "Value" "value" "text")
     [:div.actions
      [:input.btn.btn-primary {:type "submit" :value "Add"}]]]]])

;; admin-index-block

(defsection admin-index-block [AuthenticationMechanism :html]
  [items & [page]]
  [:table.table
   [:thead
    [:tr
     [:th "Id"]
     [:th "User"]
     [:th "Value"]
     [:th "Actions"]]]
   [:tbody (when *dynamic* {:data-bind "foreach: items"})
    (map #(admin-index-line % page) items)]])

;; admin-index-line

(defsection admin-index-line [AuthenticationMechanism :html]
  [item & [page]]
  [:tr {:data-model "authentication-mechanism"}
   [:td
    (if *dynamic*
      {:data-bind "text: _id"}
      (:_id item))]
   [:td
    (bind-to "user"
      [:div {:data-model "user"}
       (let [user (if *dynamic*
                    (User.)
                    (model.user/fetch-by-id (:user item)))]
         (link-to user))])]
   [:td
    (if *dynamic*
      {:data-bind "text: value"}
      (:value item))]
   [:td (actions-section item)]])
