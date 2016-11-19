(ns jiksnu.directives
  (:require jiksnu.app
            [jiksnu.macros :refer-macros [list-directive item-directive]])
  (:use-macros [gyr.core :only [def.directive]]))

(def.directive jiksnu.addAlbumForm
  []
  #js
  {:controller "NewAlbumController"
   :scope true
   :templateUrl "/templates/add-album-form"})

(def.directive jiksnu.addGroupForm
  []
  #js
  {:controller "NewGroupController"
   :scope true
   :templateUrl "/templates/add-group-form"})

(def.directive jiksnu.addPictureForm
  []
  #js
  {:controller "NewPictureController"
   :scope true
   :templateUrl "/templates/add-picture-form"})

(def.directive jiksnu.addPostForm
  []
  #js
  {:controller "NewPostController"
   :scope true
   :templateUrl "/templates/add-post-form"})

(def.directive jiksnu.addStreamForm []
  #js
  {:controller "NewStreamController"
   :scope true
   :templateUrl "/templates/add-stream-form"})

(def.directive jiksnu.asModel []
  #js
  {:controller "AsModelController"
   :template "<span ng-transclude></span>"
   :scope #js {:id "@" :model "@"}
   :transclude true})

(def.directive jiksnu.debug []
  #js
  {:controller "DebugController"
   :scope #js {:expr "=expr"
               :exprText "@expr"}
   :templateUrl "/templates/debug"})

(def.directive jiksnu.displayAvatar []
  #js
  {:controller "DisplayAvatarController"
   :scope #js {:id "@" :size "@"}
   :templateUrl "/templates/display-avatar"})

(def.directive jiksnu.followButton
  []
  #js
  {:controller "FollowButtonController"
   :scope #js {:item "="}
   :templateUrl "/templates/follow-button"})

(def.directive jiksnu.mainLayout []
  #js
  {:templateUrl "/templates/main-layout"
   :controller "MainLayoutController"})


(def.directive jiksnu.leftColumn []
  #js
  {:controller "LeftColumnController"
   :scope true
   :templateUrl "/templates/left-column-section"})

(list-directive "Activities"    "activities")
(list-directive "Albums"        "albums")
(list-directive "Followers"     "followers")
(list-directive "Following"     "following")
(list-directive "Groups"        "groups")
(list-directive "GroupAdmins"   "group-admins")
(list-directive "GroupMembers"  "group-members")
(list-directive "Likes"         "likes")
(list-directive "Notifications" "notifications")
(list-directive "Pictures"      "pictures")
(list-directive "Services"      "services")
(list-directive "Streams"       "streams")

(def.directive jiksnu.navBar []
  #js
  {:controller "NavBarController"
   :scope true
   :templateUrl "/templates/navbar-section"})

(item-directive "Activity"               "activity")
(item-directive "Album"                  "album")
(item-directive "AlbumMinimal"           "album-minimal")
(item-directive "Client"                 "client")
(item-directive "ClientMinimal"          "client-minimal")
(item-directive "Conversation"           "conversation")
(item-directive "Group"                  "group")
(item-directive "GroupMembership"        "group-membership")
(item-directive "GroupMembershipMinimal" "group-membership-minimal")
(item-directive "GroupMinimal"           "group-minimal")
(item-directive "FollowersMinimal"       "followers-minimal")
(item-directive "FollowingMinimal"       "following-minimal")
(item-directive "Like"                   "like")
(item-directive "LikedBy"                "liked-by")
(item-directive "Notification"           "notification")
(item-directive "Picture"                "picture")
(item-directive "PictureMinimal"         "picture-minimal")
(item-directive "RequestToken"           "request-token")
(item-directive "Service"                "service")
(item-directive "StreamMinimal"          "stream-minimal")
(item-directive "Subscription"           "subscription")
(item-directive "User"                   "user")
(item-directive "UserMinimal"            "user-minimal")

(def.directive jiksnu.sidenav []
  #js
  {:templateUrl "/templates/sidenav"
   :controller "SidenavController"})

(def.directive jiksnu.spinner []
  #js
  {:templateUrl "/templates/spinner"})

(def.directive jiksnu.subpage []
  #js
  {:scope #js {:subpage "@name" :item "=item"}
   :template "<div ng-transclude></div>"
   :transclude true
   :controller "SubpageController"})
