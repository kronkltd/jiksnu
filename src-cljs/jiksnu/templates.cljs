(ns jiksnu.templates
  (:require [hipo :as hipo :include-macros true]))

(defn control-line
  [label name type & {:as options}]
  (let [{:keys [value checked]} options]
    [:div.control-group
     [:label.control-label {:for name} label]
     [:div.controls
      [:input
       (merge {:type type :name name}
              (when value
                {:value value})
              (when checked
                {:checked "checked"}))]]]))

(def login-page
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
       [:input.btn.primary {:type "submit" :value "Login"}]]]]]])

(def register-page
  [:form.well.form-horizontal.register-form
   {:method "post" :action "/main/register"}
   [:fieldset
    [:legend "Register"]
    (map
     (fn [[label field type]]
       (control-line label field type))
     [["Username"               "username"         "text"]
      ["Password"               "password"         "password"]
      ["Confirm Password"       "confirm-password" "password"]
      ["Email"                  "email"            "email"]
      ["Display Name"           "display-name"     "text"]
      ["Location"               "location"         "text"]
      ["I have checked the box" "accepted"         "checkbox"]])
    [:div.actions
     [:input.btn.primary {:type "submit" :value "Register"}]]]])

(def streams-widget
  [:div
   [:h3 "Streams {{page.totalRecords}}"]
   [:ul
    [:li {:ng-repeat "stream in streams"}
     "{{stream.name}}"]]])

(def admin-streams
  [:table.table
   [:thead
    [:tr
     [:th "Name"]]]
   [:tbody
    [:tr {:data-model "stream"
          :ng-repeat "conversation in conversations"}
     [:td "{{conversation.name}}"]]]])

(defn add-stream-form
  [user]
  [:form {:method "post"
          :action "/users/{{user.id}}/streams"}
   [:input {:type "text" :name "name"}]
   [:input {:type "submit"}]])

(def navbar-section
  [:nav.navbar.navbar-default.navbar-inverse
   {:role "navigation"}
   [:div.container-fluid
    [:div.navbar-header
     [:button.navbar-toggle
      {:type "button"
       :data-toggle "collapse"
       :data-target "#main-navbar-collapsw-1"}
      [:span.sr-only "Toggle Navigation"]
      [:span.icon-bar]
      [:span.icon-bar]
      [:span.icon-bar]]
     [:a.navbar-brand.home {:href "/" :rel "top"}
      "{{app.name}}"]]
    [:div#main-navbar-collapse-1.navbar-collapse.collapse
     [::ul.nav.navbar-nav.navbar-right {:ng-if "app.user"}
      [:li.dropdown
       [:a.dropdown-toggle (merge {:href "#" :data-toggle "dropdown"})
        [:span
         [:span {:data-model "user"}
          [:show-avatar {:data-id "{{app.user}}"}]
          "{{app.user}}"
          [:link-to {:data-id "{{app.user}}" :data-model "user"}]]]
        [:b.caret]]
       [:ul.dropdown-menu
        [:li
         [:a.settings-link {:href "/main/settings"} "Settings"]
         [:a.profile-link {:href "/main/profile"} "Profile"]
         [:a.logout-link {:href "/main/logout?_method=POST"
                          :target "_self"} "Log out"]]]]]
     [::ul.nav.navbar-nav.navbar-right {:ng-if "!app.user"}
      [:li.unauthenticated [:a.login-link {:ui-sref "loginPage"} "Login"]]
      [:li.divider-vertical]
      [:li [:a.register-link {:href "/main/register"} "Register"]]]]]]
  )

(def left-column-section
  [:div#mainNav
   [:p "Nav"]
   [:ul.nav.nav-list
    [:li {:ng-repeat "group in groups"}
     [:li.nav-header "{{group.label}}"]
     [:ul
      [:li {:ng-repeat "item in group.items"}
       ;; "<!--  -->"
       [:span {:ng-if "item.state"}
        [:a {:ui-sref "{{item.state}}"}
         "{{item.title}}"]]
       [:span {:ng-if "!item.state"}
        "stateless"
        ]
       ;; "<!-- /ngIf -->"
       ]]]]
   [:hr]
   [:div
    [:h3 "Formats"]
    [:ul.unstyled
     [:li.format-line
      {:ng-repeat "format in formats"}
      [:a {:href "{{format.href}}"}
       [:span.format-icon
        [:img {:alt ""
               :ng-src "/themes/classic/{{format.icon}}"}]]
       [:span.format-label "{{format.label}}"]]]]]])
