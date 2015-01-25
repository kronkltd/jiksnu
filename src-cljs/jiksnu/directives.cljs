(ns jiksnu.directives
  (:require jiksnu.app
            [jiksnu.controllers :as controller])
  (:use-macros [gyr.core :only [def.directive]]
               [purnam.core :only [! ? arr obj]]))

(def.directive jiksnu.addPostForm
  []
  (obj
   :templateUrl "/templates/add-post-form"
   :scope true
   :controller "NewPostController"))

(def.directive jiksnu.addStreamForm []
  (obj))

(def.directive jiksnu.addWatcherForm []
  (obj))

(def.directive jiksnu.displayAvatar
  []
  (obj
   :templateUrl "/templates/display-avatar"
   :link (fn [$scope element attrs]
           (.init $scope (.-id attrs))
           (.$watch $scope (fn [] (.-id attrs))
                    (fn [newVal oldVal]
                      (.init $scope newVal))))
   :scope true
   :controller "DisplayAvatarController"))

(def.directive jiksnu.followersList
  []
  (obj
   :templateUrl "/templates/followers-list"
   :scope true
   :controller "FollowersListController"))

(def.directive jiksnu.followingList
  []
  (obj
   :templateUrl "/templates/following-list"
   :scope true
   :controller "FollowingListController"))

(def.directive jiksnu.groupsWidget
  []
  (obj
   :templateUrl "/templates/groups-widget"
   :scope true))

(def.directive jiksnu.leftColumn []
  (obj
   :templateUrl "/templates/left-column-section"
   :scope true
   :controller "LeftColumnController"))

(def.directive jiksnu.navBar []
  (obj
   :templateUrl "/templates/navbar-section"
   :scope true
   :controller "NavBarController"))

(def.directive jiksnu.rightColumn []
  (obj
   :templateUrl "/templates/right-column-section"
   :scope true
   :controller "RightColumnController"))

(def.directive jiksnu.showActivity
  []
  (obj
   :templateUrl "/templates/show-activity"
   :scope (obj)
   :controller controller/jiksnu_ShowActivityController
   :link (fn [$scope element attrs]
           (let [id (.-id attrs)]
             (.init $scope id)))))


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
   :scope true
   )
  )
