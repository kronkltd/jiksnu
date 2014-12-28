(ns jiksnu.helpers
  (:require [hipo :as hipo :include-macros true]
            [jiksnu.templates :as templates])
  (:use-macros [purnam.core :only [! obj]]))

(defn template-string
  [tmpl]
  (.-outerHTML (hipo/create tmpl)))

(defn add-states
  [$stateProvider data]
  (doseq [[state uri controller template] data]
    (.state $stateProvider
            (obj
             :name state
             :url uri
             :controller (str controller "Controller")
             :template (template-string template)))))

(defn fetch-page
  [$scope $http url]
  (fn []
    (-> $http
        (.get url)
        (.success
         (fn [data]
           (! $scope.page data))))))

(def nav-info
  [{:label "Home"
    :items
    [{:title "Public"    :state "home"}
     {:title "Users"     :state "indexUsers"}
     {:title "Feeds"     :state "indexFeedSources"}
     {:title "Domains"   :state "indexDomains"}
     {:title "Groups"    :state "indexGroups"}
     {:title "Resources" :state "indexResources"}]}
   {:label "Settings"
    :items
    [{:title "Settings" :state "settingsPage"}]}
   {:label "Admin"
    :items
    [{:title "Activities"    :state "adminActivities"}
     {:title "Auth"          :state "adminAuthentication"}
     {:title "Clients"       :state "adminClients"}
     {:title "Conversations" :state "adminConversations"}
     {:title "Feed Sources"  :state "adminSources"}
     ;; {:href "/admin/feed-subscriptions"
     ;;  :title "Feed Subscriptions"}
     {:title "Groups" :state "adminGroups"}
     ;; {:href "/admin/group-memberships"
     ;;  :title "Group Memberships"}
     ;; {:href "/admin/keys"
     ;;  :title "Keys"}
     ;; {:href "/admin/likes"
     ;;  :title "Likes"}
     ;; {:href "/admin/request-tokens"
     ;;  :title "Request Tokens"}
     ;; {:href "/admin/streams"
     ;;  :title "Streams"}
     ;; {:href "/admin/subscriptions"
     ;;  :title "Subscriptions"}
     ;; {:href "/admin/users"
     ;;  :title "Users"}
     ;; {:href "/admin/workers"
     ;;  :title "Workers"}
     ]}]
  )

(def states
  [["adminConversations" "/admin/conversations" "AdminConversation" templates/admin-conversations]

   ["avatarPage"     "/main/avatar"    "AvatarPage"         templates/avatar-page]
   ["home"           "/"               "IndexConversations" templates/public-timeline]
   ["indexDomains"   "/main/domains"   "IndexDomains"       templates/index-domains]
   ["indexFeedSources"    "/main/feed-sources"    "IndexFeedSources"        templates/index-feed-sources]
   ["indexGroups"    "/main/groups"    "IndexGroups"        templates/index-groups]
   ["indexResources" "/main/resources" "IndexResources"     templates/index-resources]
   ["indexUsers"     "/main/users"     "IndexUsers"         templates/index-users]
   ["loginPage"      "/main/login"     "LoginPage"          templates/login-page]
   ["registerPage"   "/main/register"  "RegisterPage"       templates/register-page]
   ["settingsPage"   "/main/settings"  "SettingsPage"       templates/settings-page]
   ["showActivity"   "/notice/:id"     "ShowActivity"       templates/show-activity]
   ["showDomains"    "/main/domains/:id" "ShowDomain"       templates/show-domain]
   ["showUser"       "/main/users/:id" "ShowUser" templates/show-user]
   ])
