(ns jiksnu.registry)

(def pallete-color (or (some-> js/window .-themeColor) "blue"))

(def initial-plugins
  ["angular-clipboard"
   "angularMoment"
   ;; "btford.markdown"
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
  [["avatarPage"            "/main/avatar"             [:avatar-page]]
   ["home"                  "/"                        [:public-timeline]]
   ["indexActivities"       "/main/activities"         [:index-activities]]
   ["indexAlbums"           "/main/albums"             [:index-albums]]
   ["indexClients"          "/main/clients"            [:index-clients]]
   ["indexDomains"          "/main/domains"            [:index-domains]]
   ["indexFeedSources"      "/main/feed-sources"       [:index-feed-sources]]
   ["indexGroups"           "/main/groups"             [:index-groups]]
   ["indexGroupMemberships" "/main/group-memberships"  [:index-group-memberships]]
   ["indexLikes"            "/main/likes"              [:index-likes]]
   ["indexNotifications"    "/main/notifications"      [:index-notifications]]
   ["indexPictures"         "/main/pictures"           [:index-pictures]]
   ["indexResources"        "/main/resources"          [:index-resources]]
   ["indexServices"         "/main/services"           [:index-services]]
   ["indexStreams"          "/main/streams"            [:index-streams]]
   ["indexSubscriptions"    "/main/subscriptions"      [:index-subscriptions]]
   ["indexUsers"            "/main/users"              [:index-users]]
   ["loginPage"             "/main/login"              [:login-page]]
   ["registerPage"          "/main/register"           [:register-page]]
   ["settingsPage"          "/main/settings"           [:settings-page]]
   ["showActivity"          "/main/activities/:_id"    [:show-activity {:data-id "{{$stateParams._id}}"}]]
   ["showAlbum"             "/main/albums/:_id"        [:show-album {:data-id "{{$stateParams._id}}"}]]
   ["showConversation"      "/main/conversations/:_id" [:show-conversation {:data-id "{{$stateParams._id}}"}]]
   ["showDomain"            "/main/domains/:_id"       [:show-domain {:data-id "{{$stateParams._id}}"}]]
   ["showGroup"             "/main/groups/:_id"        [:show-group {:data-id "{{$stateParams._id}}"}]]
   ["showLike"              "/main/likes/:_id"         [:show-like {:data-id "{{$stateParams._id}}"}]]
   ["showService"           "/main/services/:_id"      [:show-service {:data-id "{{$stateParams._id}}"}]]
   ["showStream"            "/main/streams/:_id"       [:show-stream {:data-id "{{$stateParams._id}}"}]]
   ["showUser"              "/main/users/:_id"         [:show-user {:data-id "{{$stateParams._id}}"}]]
   ["authorizeClient"       "/oauth/authorize"         [:authorize-client]]])

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

(def sidenav-data
  [["Home"          "home"]
   ["Activities"    "indexActivities"]
   #_["Domains"       "indexDomains"]
   ["Groups"        "indexGroups"]
   ["Likes"         "indexLikes"]
   ["Albums"        "indexAlbums"]
   ["Notifications" "indexNotifications"]
   ["Pictures"      "indexPictures"]
   #_["Services"      "indexServices"]
   #_["Streams"       "indexStreams"]
   ["Users"         "indexUsers"]
   #_["Settings"      "settingsPage"]
   #_["Profile"       "profile"]])
