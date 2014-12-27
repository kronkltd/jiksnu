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

(def add-group-form
  [:form.well.form-horizontal {:method "post" :action "/main/groups"}
   [:fieldset
    [:legend "Add a Group"]
    (control-line "Nickname" "nickname" "text")
    (control-line "Full Name" "fullname" "text")
    (control-line "Homepage" "homepage" "text")
    [:div.control-group
     [:label {:for "description"} "Description"]
     [:div.controls
      [:textarea {:name "description"}]]]
    (control-line "Location" "location" "text")
    (control-line "Aliases" "aliases" "text")
    [:div.controls
     [:input.btn.btn-primary {:type "submit" :value "Add"}]]]]
  )

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

(def admin-sources
  [:table.table
   [:thead
    [:tr
     [:th "Id"]
     [:th "Title"]
     [:th "Domain"]
     [:th "Topic"]
     [:th "Status"]
     [:th "Actions"]]]
   [:tbody
    [:tr {:ng-repeat "source in page.items"}
     [:td
      [:jiksnu-link-to {:data-id "{{source.id}}" :data-model "FeedSource"}]]
     [:td
      [:a {:title "{{source.title}}" :ui-sref "adminSource(source)"}
       "{{source.title}}"]]
     [:td "{{source.domain}}"]
     [:td
      [:a {:href "{{source.topic}}"}
       "{{source.topic}}"]]
     [:td "{{source.status}}"]
     [:td #_(actions-section item)]]]])

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

(def index-domains
  [:table.domains.table
   [:thead
    [:tr
     [:th "Name"]
     [:th "HTTP"]
     [:th "HTTPS"]
     [:th "XMPP?"]
     [:th "Discovered"]
     [:th "Host Meta"]
     [:th "# Links"]
     ]]
   [:tbody
    [:tr {:data-model "domain"
          :ng-repeat "domain in page.items"}
     [:td
      #_(favicon-link domain)
      #_(link-to domain)
      ]
     [:td "{{domain.http}}"]
     [:td "{{domain.https}}"]
     [:td "{{domain.xmpp}}"]
     [:td "{{domain.discovered}}"]
     [:td
      [:a {:href "http://{{domain._id}}/.well-known/host-meta"}
       "Host-Meta"]]
     [:td "{{domain.links.length}}"]
     [:th #_(actions-section domain)]]]]
  )

(def index-groups
  [:ul.profiles
   [:li {:ng-repeat "group in page.items"}
    [:section.profile.hentry.vcard
     {:data-model "group"}
     [:p
      [:a.url.entry-title
       {:href "/main/groups/{{group.nickname}}"}
       [:img {:ng-src "{{group.avatarUrl}}"}]
       [:span.nickname
        "{{group.fullname}} ({{group.nickname}})"]]]
     [:a.url {:href "{{group.homepage}}"}
      "{{group.homepage}}"]
     [:p.note "{{group.description}}"]]]]
  )

(def index-resources
  [:table.table
   [:thead
    [:tr
     [:th "Id"]
     [:th "Domain"]
     [:th "Url"]
     [:th "Status"]
     [:th "Content Type"]
     [:th "Encoding"]
     [:th "Requires Auth"]
     [:th "Updated"]]]
   [:tbody
    [:tr {:data-model "resource"
          :ng-repeat "resource in page.items"}
     [:td
      [:jiksnu-link-to {:data-id "{{resource.id}}" :data-model "Resource"}]]
     [:td "{{resource.domain}}"]
     [:td
      [:a {:href "{{resource.url}}"}
       "{{resource.url}}"]]
     [:td "{{resource.status}}"]
     [:td "{{resource.contentType}}"]
     [:td "{{resource.encoding}}"]
     [:td "{{resource.requiresAuth}}"]
     [:td "{{resource.updated}}"]
     [:td #_(actions-section item)]]]]
  )

(def index-sources
  [:div
   [:jiksnu-pagination-section]
   [:table.table.feed-sources
    [:thead
     [:tr
      [:th "Title"]
      [:th "Domain"]
      [:th "Topic"]
      [:th "Hub"]
      #_[:th "Mode"]
      [:th "Status"]
      [:th "Watchers"]
      [:th "Updated"]
      [:th "Actions"]]]
    [:tbody
     [:tr {:ng-repeat "source in page.items"}
      [:td
       [:jiksnu-link-to {:data-id "{{source.id}}" :data-model "FeedSouce"}]]
      [:td "{{source.domain}}"]
      [:td
       [:a {:href "{{source.topic}}"}
        "{{source.topic}}"]]
      [:td "{{source.hub}}"]
      #_[:td "{{source.mode}}"]
      [:td "{{source.status}}"
       ]
      [:td "{{source.watchers.length}}"]
      [:td "{{source.updated}}"]
      [:td
       #_(actions-section source)]]]]]
  )

(def index-users
  [:div
   [:h1 "Index Users"]
   ]
  )

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

(defn config [& _]
  (.warn js/console "config called"))

(def settings-page
  [:div
   [:h1 "Settings"]
  [:form.well.form-horizontal
   {:method "post" :action "/admin/settings"}
   [:fieldset
    [:legend "Settings Page"]
    (control-line "Site Name"
                  "site.name" "text"
                  :value (config :site :name))
    (control-line "Domain"
                  "domain" "text"
                  :value (config :domain))
    (control-line "Admin Email"
                  "site.email" "text"
                  :value (config :site :email))
    (control-line "Brought By Name"
                  "site.brought-by.name" "text"
                  :value (config :site :brought-by :name))
    (control-line "Brought By Url"
                  "site.brought-by.url" "text"
                  :value (config :site :brought-by :url))
    (control-line "Print Actions"
                  "print.actions" "checkbox"
                  :checked (config :print :actions))
    (control-line "Print Request"
                  "print.request" "checkbox"
                  :checked (config :print :request))
    (control-line "Print Routes"
                  "print.routes" "checkbox"
                  :checked (config :print :routes))
    (control-line "Print Triggers"
                  "print.triggers" "checkbox"
                  :checked (config :print :triggers))
    (control-line "Allow registration?"
                  "registration-enabled" "checkbox"
                  :checked (config :registration-enabled))
    (control-line "Private"
                  "site.private" "checkbox"
                  :checked (config :site :private))
    (control-line "Closed"
                  "site.closed" "checkbox"
                  :checked (config :site :closed))
    (control-line "Limit"
                  "site.limit.text" "text"
                  :value (config :site :limit :text))
    (control-line "Dupe"
                  "site.limit.dupe" "text"
                  :value (config :site :limit :dupe))
    (control-line "Swank Port"
                  "swank.port" "text"
                  :value (config :swank :port))
    (control-line "HTML only?"
                  "htmlOnly" "checkbox"
                  :checked (config :htmlOnly))
    [:div.actions
     [:input {:type "submit"}]]]]])

(def show-domain
  ;; (let [sc (:statusnet-config domain)
  ;;       license (:license sc)]

    [:div {:data-model "domain"}
     #_(actions-section domain)
     [:table.table
      [:thead]
      [:tbody
       [:tr
        [:th "Id"]
        [:td
         #_(favicon-link domain)
         [:span.domain-id "{{domain._id}}"]]]
       [:tr
        [:th "XMPP"]
        [:td "{{domain.xmpp}}"]]
       [:tr
        [:th "Discovered"]
        [:td "{{domain.discovered}}"]]
       [:tr
        [:th "Created"]
        [:td "{{domain.created}}"]]
       [:tr
        [:th "Updated"]
        [:td "{{domain.updated}}"]]
       [:tr
        [:th "Closed"]
        [:td #_(-> sc :site :closed)]]
       [:tr
        [:th "Private"]
        [:td #_(-> sc :site :private)]]
       [:tr
        [:th "Invite Only"]
        [:td #_(-> sc :site :inviteonly)]]
       [:tr
        [:th "Admin"]
        [:td #_(-> sc :site :email)]]
       [:tr
        [:th "License"]
        [:td
         ;; RDFa
         [:a {:href "{{domain.license.url}}"
              :title "{{domain.license.title}}"}
          [:img {:src "" #_(:image license)
                 :alt "" #_(:title license)}]]]]]]
     #_(when-let [links [{}]]
       (bind-to "links"
                (sections.link/index-section links)))]
    ;; )
  )

(def show-feed-source
  [:div {:data-model "feed-source"}
   #_(actions-section source)
   [:table.table
    [:tbody
     [:tr
      [:th "Topic:"]
      [:td [:a {:href "{{source.topic}}"}
            "{{source.topic}}"]]]
     [:tr
      [:th "Domain:"]
      [:td "{{source.domain}}"]]
     [:tr
      [:th "Hub:"]
      [:td [:a {:href "{{hub}}"}
            "{{hub}}"]]]
     [:tr
      [:th "Callback:"]
      [:td "{{source.callback}}"]]
     [:tr
      [:th  "Challenge:"]
      [:td "{{source.challenge}}"]]
     [:tr
      [:th "Mode:"]
      [:td "{{source.mode}}"]]
     [:tr
      [:th "Status:"]
      [:td "{{source.status}}"]]
     [:tr
      [:th "Verify Token:"]
      [:td "{{source.verifyToken}}"]]
     [:tr
      [:th "Created:"]
      [:td "{{source.created}}"]]
     [:tr
      [:th "Updated:"]
      [:td "{{source.updated}}"]]
     [:tr
      [:th "Lease Seconds:"]
      [:td "{{source.leaseSeconds}}"]]]]])

(def show-resource
  [:div #_(actions-section item)
   [:table.table
    [:tbody
     [:tr
      [:th "Id"]
      [:td ]]
     [:tr
      [:th "Title"]
      [:td "{{resource.title}}"]]
     [:tr
      [:th "Url"]
      [:td
       [:a {:href "{{resource.url}}"}
        "{{resource.url}}"]]]
     [:tr
      [:th "Status"]
      [:td "{{resource.status}}"]]
     [:tr
      [:th "Location"]
      [:td "{{resource.location}}"]]
     [:tr
      [:th "Content Type"]
      [:td "{{resource.contentType}}"]]
     [:tr
      [:th "Encoding"]
      [:td "{{resource.encoding}}"]]
     [:tr
      [:th "Created"]
      [:td "{{resource.created}}"]]
     [:tr
      [:th "Updated"]
      [:td "{{resource.updated}}"]]]]
   #_(sections.link/index-section links)])

(def streams-widget
  [:div
   [:h3 "Streams {{page.totalRecords}}"]
   [:ul
    [:li {:ng-repeat "stream in streams"}
     "{{stream.name}}"]]])

