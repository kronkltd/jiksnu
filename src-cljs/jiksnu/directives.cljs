(ns jiksnu.directives
  (:require jiksnu.app
            [jiksnu.controllers :as controller])
  (:use-macros [gyr.core :only [def.directive]]
               [jiksnu.macros :only [list-directive]]
               [purnam.core :only [! ? arr obj]]))

(def.directive jiksnu.addPostForm
  []
  (obj
   :controller "NewPostController"
   :scope true
   :templateUrl "/templates/add-post-form"))

(def.directive jiksnu.addStreamForm []
  (obj))

(def.directive jiksnu.addWatcherForm []
  (obj))

(def.directive jiksnu.displayAvatar
  []
  (obj
   :controller "DisplayAvatarController"
   :link (fn [$scope element attrs]
           (.init $scope (.-id attrs))
           (.$watch $scope
                    #(.-id attrs)
                    #(.init $scope %)))
   :scope true
   :templateUrl "/templates/display-avatar"))

(def.directive jiksnu.followButton
  []
  (obj
   :controller "FollowButtonController"
   :scope (obj :item "=")
   :templateUrl "/templates/follow-button"))

(def.directive jiksnu.groupsWidget
  []
  (obj
   :scope true
   :templateUrl "/templates/groups-widget"))

(def.directive jiksnu.leftColumn []
  (obj
   :controller "LeftColumnController"
   :scope true
   :templateUrl "/templates/left-column-section"))

(list-directive "Followers" "followers")
(list-directive "Following" "following")
(list-directive "Groups" "groups")
(list-directive "Streams" "streams")

(def.directive jiksnu.navBar []
  (obj
   :controller "NavBarController"
   :scope true
   :templateUrl "/templates/navbar-section"))

(def.directive jiksnu.rightColumn []
  (obj
   :controller "RightColumnController"
   :scope true
   :templateUrl "/templates/right-column-section"))

(def.directive jiksnu.showActivity
  []
  (obj
   :controller "ShowActivityController"
   :link (fn [$scope element attrs]
           (let [id (.-id attrs)]
             (.init $scope id)))
   :scope (obj)
   :templateUrl "/templates/show-activity"))

(def.directive jiksnu.showStreamMinimal
  []
  (obj
   :templateUrl "/templates/show-stream-minimal"
   :scope (obj :streamId "@" :stream "=")
   :controller "ShowStreamMinimalController"))

(def.directive jiksnu.streamsWidget []
  (obj))

(def.directive jiksnu.subscribersWidget []
  (obj
   :templateUrl "/templates/subscribers-widget"
   :scope true
   :controller "SubscribersWidgetController"))

(def.directive jiksnu.subscriptionsWidget
  []
  (obj
   :templateUrl "/templates/subscriptions-widget"
   :scope true))
