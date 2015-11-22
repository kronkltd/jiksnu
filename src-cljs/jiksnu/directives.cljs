(ns jiksnu.directives
  (:require jiksnu.app
            [jiksnu.controllers :as controller]
            [taoensso.timbre :as timbre])
  (:use-macros [gyr.core :only [def.directive]]
               [jiksnu.macros :only [list-directive]]
               [purnam.core :only [! ? arr obj]]))

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
  (obj))

(def.directive jiksnu.debug []
  #js {:controller "DebugController"
       :scope #js {:expr "=expr"
                   :exprText "@expr"}
       :templateUrl "/templates/debug"})

(def.directive jiksnu.displayAvatar []
  #js {:controller "DisplayAvatarController"
       :link (fn [$scope element attrs]
               (.init $scope (.-id attrs))
               (.$watch $scope
                        #(.-id attrs)
                        #(.init $scope %)))
       :scope true
       :templateUrl "/templates/display-avatar"})

(def.directive jiksnu.followButton
  []
  #js
  {:controller "FollowButtonController"
   :scope (obj :item "=")
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

(list-directive "Followers" "followers")
(list-directive "Following" "following")
(list-directive "Groups" "groups")
(list-directive "Streams" "streams")

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

(def.directive jiksnu.showActivity
  []
  #js
  {:controller "ShowActivityController"
   :scope (obj :id "@" :item "=")
   :templateUrl "/templates/show-activity"})

(def.directive jiksnu.showStreamMinimal
  []
  (obj
   :templateUrl "/templates/show-stream-minimal"
   :scope (obj :id "@" :item "=")
   :controller "ShowStreamMinimalController"))

(def.directive jiksnu.showUser
  []
  (obj
   :controller "ShowUserController"
   :scope (obj :id "@" :item "=")
   :templateUrl "/templates/show-user"))

(def.directive jiksnu.streamsWidget []
  (obj))

(def.directive jiksnu.subpage []
  #js
  {:scope #js {:subpage "@name" :item "=item"}
   :template "<div ng-transclude></div>"
   :transclude true
   :controller "SubpageController"})

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
