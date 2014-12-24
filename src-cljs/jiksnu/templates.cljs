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



(def add-feed-source-form
  [:form.well.form-horizontal {:method "post" :action "/admin/feed-sources"}
   [:fieldset
    [:legend "Add Source"]
    (control-line "Topic"  "topic" "text")
    (control-line "Callback" "callback" "text")
    (control-line "Challenge" "challenge" "text")
    ;; TODO: radio buttons?
    (control-line "Mode" "mode" "text")
    (control-line "User" "user" "text")
    [:div.form-actions
     [:button.btn.btn-primary
      {:type "submit"} "Add"]]]])

(def add-stream-form
  [:form {:method "post"
          :action "/users/{{user.id}}/streams"}
   [:input {:type "text" :name "name"}]
   [:input {:type "submit"}]])

(def add-watcher-form
  [:form.well.form-horizontal
   {:method "post"
    :action "/admin/feed-sources/{{source.id}}/watchers"}
   [:fieldset
    [:legend "Add Watcher"]
    (control-line "Acct id"
                  :user_id "text")
    [:input {:type "submit"}]]])

(def admin-streams
  [:table.table
   [:thead
    [:tr
     [:th "Name"]]]
   [:tbody
    [:tr {:data-model "stream"
          :ng-repeat "conversation in conversations"}
     [:td "{{conversation.name}}"]]]])

(def avatar-page
  [:form {:method "post" :action "/settings/avatar"}
   [:fieldset
    [:legend "Upload Avatar"]
    [:div.clearfix
     [:label {:for "avatar"} "Image"]
     [:div.input
      [:input {:type "file" :name "avatar"}]]]
    [:div.actions
     [:input {:type "submit" :value "Submit"}]]]])

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
      [:li.dropdown {:dropdown ""}
       [:a.dropdown-toggle
        {:dropdown-toggle ""}
        [:span {:data-model "user"}
         [:show-avatar {:data-id "{{app.user}}"}]
         "{{app.user}}"
         [:link-to {:data-id "{{app.user}}" :data-model "user"}]]
        [:b.caret]]
       [:ul.dropdown-menu {:role "menu"}
        [:li [:a {:ui-sref "settings"} "Settings"]]
        [:li [:a {:ui-sref "profile"}  "Profile"]]
        [:li [:a {:ui-sref "logout"}   "Log out"]]]]]
     [::ul.nav.navbar-nav.navbar-right {:ng-if "!app.user"}
      [:li [:a {:ui-sref "loginPage"} "Login"]]
      [:li.divider-vertical]
      [:li [:a {:ui-sref "register"} "Register"]]]]]])

(def left-column-section
  [:div#mainNav
   [:div.panel.panel-default {:ng-repeat "group in groups"}
    [:div.panel-heading.text-center "{{group.label}}"]
    [:div.list-group
     [:a.list-group-item
      {:ng-repeat "item in group.items"
       :ng-if "item.state"
       :ui-sref "{{item.state}}"}
      "{{item.title}}"]]]
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
