(ns jiksnu.registry)

(def initial-plugins
  ["angular-clipboard"
   "angularMoment"
   "btford.markdown"
   "cfp.hotkeys"
   "geolocation"
   "hljs"
   "js-data"
   "lfNgMdFileInput"
   "ngMaterial"
   "ngMdIcons"
   "ngSanitize"
   "ngWebSocket"
   "ui.router"
   "ui.select"])

(def route-data
  [["avatarPage"            "/main/avatar"             "AvatarPage"            :avatar-page]
   ["home"                  "/"                        "IndexConversations"    :public-timeline]
   ["indexActivities"       "/main/activities"         "IndexActivities"       :index-activities]
   ["indexAlbums"           "/main/albums"             "IndexAlbums"           :index-albums]
   ["indexClients"          "/main/clients"            "IndexClients"          :index-clients]
   ["indexDomains"          "/main/domains"            "IndexDomains"          :index-domains]
   ["indexFeedSources"      "/main/feed-sources"       "IndexFeedSources"      :index-feed-sources]
   ["indexGroups"           "/main/groups"             "IndexGroups"           :index-groups]
   ["indexGroupMemberships" "/main/group-memberships"  "IndexGroupMemberships" :index-group-memberships]
   ["indexLikes"            "/main/likes"              "IndexLikes"            :index-likes]
   ["indexNotifications"    "/main/notifications"      "IndexNotifications"    :index-notifications]
   ["indexPictures"         "/main/pictures"           "IndexPictures"         :index-pictures]
   ["indexResources"        "/main/resources"          "IndexResources"        :index-resources]
   ["indexServices"         "/main/services"           "IndexServices"         :index-services]
   ["indexStreams"          "/main/streams"            "IndexStreams"          :index-streams]
   ["indexSubscriptions"    "/main/subscriptions"      "IndexSubscriptions"    :index-subscriptions]
   ["indexUsers"            "/main/users"              "IndexUsers"            :index-users]
   ["loginPage"             "/main/login"              "LoginPage"             :login-page]
   ["registerPage"          "/main/register"           "RegisterPage"          :register-page]
   ["settingsPage"          "/main/settings"           "SettingsPage"          :settings-page]
   ["showActivity"          "/main/activities/:_id"    "ShowActivity"          :show-activity]
   ["showAlbum"             "/main/albums/:_id"        "ShowAlbum"             :show-album]
   ["showConversation"      "/main/conversations/:_id" "ShowConversation"      :show-conversation]
   ["showDomain"            "/main/domains/:_id"       "ShowDomain"            :show-domain]
   ["showGroup"             "/main/groups/:_id"        "ShowGroup"             :show-group]
   ["showLike"              "/main/likes/:_id"         "ShowLike"              :show-like]
   ["showService"           "/main/services/:_id"      "ShowService"           :show-service]
   ["showStream"            "/main/streams/:_id"       "ShowStream"            :show-stream]
   ["showUser"              "/main/users/:_id"         "ShowUser"              :show-user]
   ["authorizeClient"       "/oauth/authorize"         "AuthorizeClient"       :authorize-client]])

(def page-mappings
  {"activities"        "/model/activities"
   "albums"            "/model/albums"
   "clients"           "/model/clients"
   "conversations"     "/model/conversations"
   "domains"           "/model/domains"
   "feed-sources"      "/model/feed-sources"
   "groups"            "/model/groups"
   "group-memberships" "/model/group-memberships"
   "likes"             "/model/likes"
   "notifications"     "/model/notifications"
   "pictures"          "/model/pictures"
   "resources"         "/model/resources"
   "services"          "/model/services"
   "streams"           "/model/streams"
   "subscriptions"     "/model/subscriptions"
   "users"             "/model/users"})

(def subpage-mappings
  {"Activity"     {"likes"      #(str "/model/activities/"    (.-_id %) "/likes")}
   "Album"        {"pictures"   #(str "/model/albums/"        (.-_id %) "/pictures")}
   "Conversation" {"activities" #(str "/model/conversations/" (.-_id %) "/activities")}
   "Group"        {"members"    #(str "/model/groups/"        (.-_id %) "/members")}
   "Stream"       {"activities" #(str "/model/streams/"       (.-_id %) "/activities")}
   "User"         {"activities" #(str "/model/users/"         (.-_id %) "/activities")
                   "albums"     #(str "/model/users/"         (.-_id %) "/albums")
                   "following"  #(str "/model/users/"         (.-_id %) "/following")
                   "followers"  #(str "/model/users/"         (.-_id %) "/followers")
                   "groups"     #(str "/model/users/"         (.-_id %) "/groups")
                   "streams"    #(str "/model/users/"         (.-_id %) "/streams")}})

(def hotkey-data
  [["g a" "indexActivities"       "Go to Activities"]
   ["g b" "indexAlbums"           "Go to Albums"]
   ["g c" "indexClients"          "Go to Clients"]
   ["g d" "indexDomains"          "Go to Domains"]
   ["g g" "indexGroups"           "Go to Groups"]
   ["g m" "indexGroupMemberships" "Go to Group Memberships"]
   ["g n" "indexNotifications"    "Go to Notifications"]
   ["g p" "indexPictures"         "Go to Pictures"]
   ["g h" "home"                  "Go to Home"]
   ["g l" "indexLikes"            "Go to Likes"]
   ["g s" "indexStreams"          "Go to Streams"]
   ["g u" "indexUsers"            "Go to Users"]])
