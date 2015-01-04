(ns jiksnu.modules.web.sections.auth-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section add-form index-block
                                           index-line link-to]]
            [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.core.sections :refer [admin-index-block admin-index-line]]
            [jiksnu.modules.web.sections :refer [bind-to control-line]]
            [jiksnu.modules.web.sections.user-sections :refer [display-avatar-img]])
  (:import jiksnu.model.AuthenticationMechanism
           jiksnu.model.User))

(defn ng-login-section
  []
  (list
   ))

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

(defsection add-form [AuthenticationMechanism :html]
  [mechanism & _]
  [:form.well {:method "post" :action "/admin/auth"}
   [:fieldset
    [:legend "Add a mechanism"
     (control-line "Type" "type" "text")
     (control-line "Value" "value" "text")
     [:div.actions
      [:input.btn.btn-primary {:type "submit" :value "Add"}]]]]])

(defsection admin-index-block [AuthenticationMechanism :html]
  [items & [page]]
  [:table.table
   [:thead
    [:tr
     [:th "Id"]
     [:th "User"]
     [:th "Value"]
     [:th "Actions"]]]
   [:tbody
    (map #(admin-index-line % page) items)]])

;; admin-index-line

(defsection admin-index-line [AuthenticationMechanism :html]
  [item & [page]]
  [:tr {:data-model "authentication-mechanism"
        :ng-repeat "mech in page.items"}
   [:td "{{mech.id}}"]
   [:td
    (bind-to "user"
             [:div {:data-model "user"}
              (let [user (User.)]
                (link-to user))])]
   [:td "{{mech.value}}"]
   [:td (actions-section item)]])

