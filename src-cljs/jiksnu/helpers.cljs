(ns jiksnu.helpers
  (:require [clojure.string :as string])
  (:use-macros [purnam.core :only [! obj]]))

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

;; TODO: surely this already exists
(defn hyphen-case
  [s]
  (string/lower-case
   (string/replace s #"([a-z])([A-Z])" "$1-$2")))

(defn admin-states
  [data]
  (->> data
       (mapcat
        (fn [[a c]]
          [{:state    (str "admin" a)
            :path     (str "/admin/" (hyphen-case a))
            :class    (str "Admin" a)
            :template (str "admin-" (hyphen-case a))}
           {:state    (str "admin" c)
            :path     (str "/admin/" (hyphen-case a) "/:id")
            :class    (str "Admin" c)
            :template (str "admin-" (hyphen-case c))}]))
       (map (fn [o] (mapv #(% o) [:state :path :class :template])))))

(def nav-info
  [{:label "Home"
    :items
    [{:title "Public"               :state "home"}
     {:title "Users"                :state "indexUsers"}
     {:title "Feeds"                :state "indexFeedSources"}
     {:title "Domains"              :state "indexDomains"}
     {:title "Groups"               :state "indexGroups"}
     {:title "Resources"            :state "indexResources"}]}
   #_{:label "Settings"
    :items
    [{:title "Settings"           :state "settingsPage"}]}
   #_{:label "Admin"
    :items
    [{:title "Activities"         :state "adminActivities"}
     ;; {:title "Auth"               :state "adminAuthentication"}
     ;; {:title "Clients"            :state "adminClients"}
     {:title "Conversations"      :state "adminConversations"}
     ;; {:title "Feed Sources"       :state "adminSources"}
     ;; {:title "Feed Subscriptions" :state "adminFeedSubscriptions"}
     {:title "Groups"             :state "adminGroups"}
     ;; {:title "Group Memberships"  :state "adminGroupMemberships"}
     ;; {:title "Keys"               :state "adminKeys"}
     ;; {:title "Likes"              :state "adminLikes"}
     ;; {:title "Request Tokens"     :state "adminRequestTokens"}
     ;; {:title "Streams"            :state "adminStreams"}
     ;; {:title "Subscriptions"      :state "adminSubscriptions"}
     {:title "Users"              :state "adminUsers"}
     ;; {:title "Workers"            :state "adminWorkers"}
     ]}])

(def admin-data
  [["Activities"    "Activity"]
   ["Conversations" "Conversation"]
   ["Groups"        "Group"]
   ["Resources"     "Resource"]
   ["Users"         "User"]
   ])

(def route-data
  [
   ["avatarPage"       "/main/avatar"       "AvatarPage"         :avatar-page]
   ["home"             "/"                  "IndexConversations" :public-timeline]
   ["indexDomains"     "/main/domains"      "IndexDomains"       :index-domains]
   ["indexFeedSources" "/main/feed-sources" "IndexFeedSources"   :index-feed-sources]
   ["indexGroups"      "/main/groups"       "IndexGroups"        :index-groups]
   ["indexResources"   "/main/resources"    "IndexResources"     :index-resources]
   ["indexUsers"       "/main/users"        "IndexUsers"         :index-users]
   ["loginPage"        "/main/login"        "LoginPage"          :login-page]
   ["registerPage"     "/main/register"     "RegisterPage"       :register-page]
   ["settingsPage"     "/main/settings"     "SettingsPage"       :settings-page]
   ["showActivity"     "/notice/:id"        "ShowActivity"       :show-activity]
   ["showDomain"      "/main/domains/:id"  "ShowDomain"         :show-domain]
   ["showUser"         "/main/users/:username@:domain"    "ShowUser"           :show-user]
   ]
  )

(def states
  (let [as (admin-states admin-data)]
    (concat as route-data)))
