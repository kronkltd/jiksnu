(ns jiksnu.directives
  (:require jiksnu.app
            [jiksnu.macros :refer-macros [list-directive item-directive]])
  (:use-macros [gyr.core :only [def.directive]]))

(def.directive jiksnu.addGroupForm
  []
  #js
  {:controller "NewGroupController"
   :scope true
   :templateUrl "/templates/add-group-form"})

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

(def.directive jiksnu.addWatcherForm []
  #js {})

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

(def.directive jiksnu.groupsWidget
  []
  #js
  {:scope true
   :templateUrl "/templates/groups-widget"})

(def.directive jiksnu.leftColumn []
  #js
  {:controller "LeftColumnController"
   :scope true
   :templateUrl "/templates/left-column-section"})

(list-directive "Activities" "activities")
(list-directive "Followers"  "followers")
(list-directive "Following"  "following")
(list-directive "Groups"     "groups")
(list-directive "Likes"      "likes")
(list-directive "Streams"    "streams")

(def.directive jiksnu.navBar []
  #js
  {:controller "NavBarController"
   :scope true
   :templateUrl "/templates/navbar-section"})

(def.directive jiksnu.rightColumn []
  #js
  {:controller "RightColumnController"
   :scope true
   :templateUrl "/templates/right-column-section"})

(item-directive "Activity"         "activity")
(item-directive "Conversation"     "conversation")
(item-directive "FollowersMinimal" "followers-minimal")
(item-directive "FollowingMinimal" "following-minimal")
(item-directive "Like"             "like")
(item-directive "StreamMinimal"    "stream-minimal")
(item-directive "Subscription"     "subscription")
(item-directive "User"             "user")
(item-directive "UserMinimal"      "user-minimal")

(def.directive jiksnu.spinner []
  #js
  {:templateUrl "/templates/spinner"})

(def.directive jiksnu.streamsWidget []
  #js
  {})

(def.directive jiksnu.subpage []
  #js
  {:scope #js {:subpage "@name" :item "=item"}
   :template "<div ng-transclude></div>"
   :transclude true
   :controller "SubpageController"})

(def.directive jiksnu.subscribersWidget []
  #js
  {:templateUrl "/templates/subscribers-widget"
   :scope true
   :controller "SubscribersWidgetController"})

(def.directive jiksnu.subscriptionsWidget
  []
  #js
  {:templateUrl "/templates/subscriptions-widget"
   :scope true})
