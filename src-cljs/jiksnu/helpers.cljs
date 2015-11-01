(ns jiksnu.helpers
  (:require [clojure.string :as string])
  (:use-macros [purnam.core :only [! ? obj]]
               [jiksnu.macros :only [state-hotkey]]))

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
     {:title "Resources"            :state "indexResources"}
     {:title "Streams"              :state "indexStreams"}
     ]}
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
   ["avatarPage"       "/main/avatar"             "AvatarPage"         :avatar-page]
   ["home"             "/"                        "IndexConversations" :public-timeline]
   ["indexDomains"     "/main/domains"            "IndexDomains"       :index-domains]
   ["indexFeedSources" "/main/feed-sources"       "IndexFeedSources"   :index-feed-sources]
   ["indexGroups"      "/main/groups"             "IndexGroups"        :index-groups]
   ["indexResources"   "/main/resources"          "IndexResources"     :index-resources]
   ["indexStreams"     "/main/streams"            "IndexStreams"       :index-streams]
   ["indexUsers"       "/main/users"              "IndexUsers"         :index-users]
   ["loginPage"        "/main/login"              "LoginPage"          :login-page]
   ["registerPage"     "/main/register"           "RegisterPage"       :register-page]
   ["settingsPage"     "/main/settings"           "SettingsPage"       :settings-page]
   ["showActivity"     "/notice/:id"              "ShowActivity"       :show-activity]
   ["showConversation" "/main/conversations/:_id" "ShowConversation"   :show-conversation]
   ["showDomain"       "/main/domains/:_id"       "ShowDomain"         :show-domain]
   ["showGroup"        "/main/groups/:_id"        "ShowGroup"          :show-group]
   ["showStream"       "/main/streams/:_id"       "ShowStream"         :show-stream]
   ["showUser"         "/main/users/:_id"         "ShowUser"           :show-user]
   ]
  )

(def states
  (let [as (admin-states admin-data)]
    (concat as route-data)))

(defn fetch-sub-page
  [item subpageService subpage]
  ;; (js/console.log "Fetching subpage:" item subpage)
  (-> subpageService
      (.fetch item subpage)
      (.then (fn [response] (aset item subpage (? response.body))))))

(defn init-subpage
  [$scope subpageService collection subpage]
  (set! (.-init $scope)
        (fn [item]
          (let [item (or item (.-item $scope))]
            (js/console.debug "init subpage" subpage (type item))
            (-> (.fetch subpageService item subpage)
                (.then (fn [page] (aset item subpage page)))))))

  (if-let [item (.-item $scope)]
    (.init $scope)
    (if-let [id (.-id $scope)]
      (-> (.find collection id)
          (.then (fn [item] (.init $scope item subpage))))
      (js/console.error "Couldn't determine item id"))))

(defn init-page
  [$scope $rootScope pageService subpageService page-type subpages]
  (.$on $rootScope "updateCollection"
        (fn []
          (.init $scope)))
  (! $scope.loaded false)
  (! $scope.init
     (fn []
       (js/console.debug "Loading page: " page-type)
       (-> pageService
           (.fetch page-type)
           (.then (fn [page]
                    (! $scope.page page)
                    (! $scope.loaded true)
                    (doall (map
                            (fn [item]
                              (doall (map (partial fetch-sub-page item subpageService)
                                          subpages)))
                            (? page.items)))))))))

(defn add-stream
  "Creates a new stream for the authenticated user.

  returns a promise"
  [$scope $http]
  (if-let [user (.-user $scope)]
    (if-let [stream-name (? $scope.stream.name)]
      (do
        (js/console.log "Adding Stream: " user stream-name)
        (-> (.post $http "/model/stream"
                   #js {:actor (.-_id user)
                        :name stream-name})
            (.then (fn [res]
                     (js/console.info "got response" res)))))
      (throw (js/Exception. "Could not determine stream name")))
    (throw (js/Exception. "No authenticated user"))))

(defn setup-hotkeys
  [hotkeys $state]
  (state-hotkey "g d" "indexDomains" "Go to Domains")
  (state-hotkey "g g" "indexGroups" "Go to Groups")
  (state-hotkey "g h" "home" "Go to Home")
  (state-hotkey "g s" "settingsPage" "Go to Settings")
  (state-hotkey "g u" "indexUsers" "Go to Users"))
