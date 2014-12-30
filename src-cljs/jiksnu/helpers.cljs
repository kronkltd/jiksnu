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
             :templateUrl (str "/templates/" (name template))))))

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
   #_{:label "Settings"
    :items
    [{:title "Settings" :state "settingsPage"}]}
   #_{:label "Admin"
    :items
    [{:title "Activities"    :state "adminActivities"}
     {:title "Auth"          :state "adminAuthentication"}
     {:title "Clients"       :state "adminClients"}
     {:title "Conversations" :state "adminConversations"}
     {:title "Feed Sources"  :state "adminSources"}
     {:href "/admin/feed-subscriptions"
      :title "Feed Subscriptions" :state "adminFeedSubscriptions"}
     {:title "Groups"             :state "adminGroups"}
     {:href "/admin/group-memberships"
      :title "Group Memberships" :state "adminGroupMemberships"}
     {:href "/admin/keys"
      :title "Keys"              :state "adminKeys"}
     {:href "/admin/likes"
      :title "Likes"             :state "adminLikes"}
     {:href "/admin/request-tokens"
      :title "Request Tokens" :state "adminRequestTokens"}
     {:href "/admin/streams"
      :title "Streams" :state "adminStreams"}
     {:href "/admin/subscriptions"
      :title "Subscriptions" :state "adminSubscriptions"}
     {:href "/admin/users"
      :title "Users" :state "adminUsers"}
     {:href "/admin/workers"
      :title "Workers" :state "adminWorkers"}
     ]}]
  )

(def states
  [["adminConversations" "/admin/conversations" "AdminConversation"  :admin-conversations]
   ["avatarPage"         "/main/avatar"         "AvatarPage"         :avatar-page]
   ["home"               "/"                    "IndexConversations" :public-timeline]
   ["indexDomains"       "/main/domains"        "IndexDomains"       :index-domains]
   ["indexFeedSources"   "/main/feed-sources"   "IndexFeedSources"   :index-feed-sources]
   ["indexGroups"        "/main/groups"         "IndexGroups"        :index-groups]
   ["indexResources"     "/main/resources"      "IndexResources"     :index-resources]
   ["indexUsers"         "/main/users"          "IndexUsers"         :index-users]
   ["loginPage"          "/main/login"          "LoginPage"          :login-page]
   ["registerPage"       "/main/register"       "RegisterPage"       :register-page]
   ["settingsPage"       "/main/settings"       "SettingsPage"       :settings-page]
   ["showActivity"       "/notice/:id"          "ShowActivity"       :show-activity]
   ["showDomains"        "/main/domains/:id"    "ShowDomain"         :show-domain]
   ["showUser"           "/main/users/:id"      "ShowUser"           :show-user]
   ])
