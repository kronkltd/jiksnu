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
  [userService]
  (obj
   :templateUrl "/templates/display-avatar"
   :link (fn [$scope element attrs]
           (.init $scope (.-id attrs))
           (.$watch $scope (fn [] (.-id attrs))
                    (fn [newVal oldVal]
                      (.log js/console "Watching attrs" newVal)
                      (.init $scope newVal))))
   :scope true
   :controller
   (arr "$scope"
        (fn [$scope]
          (! $scope.init
             (fn [id]
               (when (and id (not= id ""))
                 (! $scope.size 32)
                 (-> userService
                     (.get id)
                     (.then (fn [user]
                              (! $scope.user user)))))
               ))))))

(def.directive jiksnu.groupsWidget
  []
  (obj
   :templateUrl "/templates/groups-widget"
   :scope true
   )
  )

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
  [$http activityService]
  (obj
   :templateUrl "/templates/show-activity"
   :scope (obj)
   :controller controller/jiksnu_ShowActivityController
   :link (fn [$scope element attrs]
           (let [id (.-id attrs)]
             (.init $scope id)))))


(def.directive jiksnu.streamsWidget []
  (obj))

(def.directive jiksnu.subscribersWidget
  [app]
  (obj
   :templateUrl "/templates/subscribers-widget"
   :scope true
   :controller (arr "$scope" "userService"
                    (fn [$scope userService]
                      (-> userService
                          (.get (? app.data.user))
                          (.then (fn [user]
                                   (! $scope.user user))))))

   )
  )

(def.directive jiksnu.subscriptionsWidget
  []
  (obj
   :templateUrl "/templates/subscriptions-widget"
   :scope true
   )
  )
