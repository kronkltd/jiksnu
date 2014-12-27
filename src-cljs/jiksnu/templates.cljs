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

(def add-post-form
  [:form  {:ng-submit "submit()"}
   [:input {:type "hidden" :ng-model "activity.source"}]
   [:div {:collapse "form.shown"}
    [:input {:type "text" :placeholder "What are you doing?"
             :ng-click "form.shown = true"}]]
   [:div.post-form {:collapse "!form.shown"}
    [:fieldset
     [:tabset
      [:tab {:heading "Note"}
       [:legend "Post an activity"]
       (control-line "Title" "title" "text")
       [:div.control-group
        [:label.control-label {:for "content"} "Content"]
        [:div.controls
         [:textarea {:name "content" :rows "3"
                     :ng-model "activity.content"}]]]
       [:fieldset.add-buttons
        [:legend "Add:"]
        [:div.button-group
         [:label.btn {:ng-model "add.tags" :btn-checkbox ""}
          [:i.icon-tag]        [:span.button-text "Tags"]]
         [:label.btn {:ng-model "add.recipients" :btn-checkbox ""}
          [:i.icon-user]       [:span.button-text "Recipients"]]
         [:label.btn {:ng-model "add.location" :btn-checkbox ""}
          [:i.icon-map-marker] [:span.button-text "Location"]]
         [:label.btn {:ng-model "add.links" :btn-checkbox ""}
          [:i.icon-bookmark]   [:span.button-text "Links"]]
         [:label.btn {:ng-model "add.pictures" :btn-checkbox ""}
          [:i.icon-picture]    [:span.button-text "Pictures"]]]]

       [:div.control-group {:collapse "!add.pictures"}
        [:label.control-label {:for "pictures"} "Pictures"]
        [:div.controls
         [:input {:type "file" :name "pictures"}]]]
       [:div.control-group {:collapse "!add.location"}
        [:label.control-label "Location"]
        [:div.controls
         [:label {:for "geo.latitude"} "Latitude"]
         [:div.input
          [:input {:type "text" :ng-model "activity.geo.latitude"}]]
         [:label {:for "geo.longitude"} "Longitude"]
         [:div.input
          [:input {:type "text" :ng-model "activity.geo.longitude"}]]]]
       [:div.control-group {:collapse "!add.tags"}
        [:label.control-label {:for "tags"} "Tags"]
        [:div.controls
         [:input {:type "text" :name "tags" :ng-model "tag"}]
         [:a.btn {:ng-click "addTag"}
          "Add Tags"]]]]
      [:tab {:heading "Poll"}
       [:legend "Post a question"]
       (control-line "Question" "question" "text")
       (control-line "Answer" "answer[0]" "text")
       (control-line "Answer" "answer[1]" "text")
       (control-line "Answer" "answer[2]" "text")
       (control-line "Answer" "answer[3]" "text")
       (control-line "Answer" "answer[4]" "text")]
      [:tab {:heading "Event"}
       [:legend "Post an event"]
       (control-line "Title" "title" "type")]]
     [:div.actions
      [:select {:name "privacy" :ng-model "activity.privacy"}
       [:option {:value "public"} "Public"]
       [:option {:value "group"} "Group"]
       [:option {:value "custom"} "Custom"]
       [:option {:value "private"} "Private"]]
      ;; TODO: Group dropdown
      [:jiksnu-group-select]]]
    [:button.btn.pull-right {:ng-click "form.shown = false"} "Cancel"]
    [:button.btn.btn-primary.pull-right {:type "submit"} "Post"]]])

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

(def admin-groups
  [:table.table.groups
   [:thead
    [:tr
     [:th "Name"]
     [:th "Full Name"]
     [:th "Homepage"]]]
   [:tbody
    [:tr {:data-model "group"
          :ng-repeat "group in page.items"
          :data-id "{{group.id}}"}
     [:td "{{group.nickname}}"]
     [:td "{{group.fullname}}"]
     [:td "{{group.homepage}}"]
     [:td
      #_(actions-section group)]]]])

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

(def left-column-section
  [:div#mainNav
   [:div.panel.panel-default {:ng-repeat "group in groups"}
    [:div.panel-heading.text-center "{{group.label}}"]
    [:div.list-group
     [:a.list-group-item
      {:ng-repeat "item in group.items"
       ;; :ng-if "item.state"
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
         [:jiksnu-show-avatar {:data-id "{{app.user}}"}]
         "{{app.user}}"
         [:jiksnu-link-to {:data-id "{{app.user}}" :data-model "user"}]]
        [:b.caret]]
       [:ul.dropdown-menu {:role "menu"}
        [:li [:a {:ui-sref "settings"} "Settings"]]
        [:li [:a {:ui-sref "profile"}  "Profile"]]
        [:li [:a {:ui-sref "logout"}   "Log out"]]]]]
     [::ul.nav.navbar-nav.navbar-right {:ng-if "!app.user"}
      [:li [:a {:ui-sref "loginPage"} "Login"]]
      [:li.divider-vertical]
      [:li [:a {:ui-sref "register"} "Register"]]]]]])

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

(def right-column-section
  [:div
   [:h2 "Right Column"]])

(def settings-page
  [:div
   [:h1 "Settings"]])

(def streams-widget
  [:div
   [:h3 "Streams {{page.totalRecords}}"]
   [:ul
    [:li {:ng-repeat "stream in streams"}
     "{{stream.name}}"]]])

