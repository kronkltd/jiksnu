(ns jiksnu.directives
  (:require jiksnu.app)
  (:use-macros [gyr.core :only [def.directive]]
               [purnam.core :only [! arr obj]]))

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
           (let [id (.-id attrs)]
             ;; (.log js/console "linking avatar: " id)
             (.init $scope id)))
   :controller
   (arr "$scope"
        (fn [$scope]
          (! $scope.init
             (fn [id]
               (! $scope.size 32)
               (when (and id (not= id ""))
                 (-> userService
                     (.get id)
                     (.then (fn [user]
                              (! $scope.user user)))))
               ))))))

(def.directive jiksnu.groupsWidget
  []
  (obj
   :templateUrl "/templates/groups-widget"
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
   :controller
   (arr "$scope"
        (fn [$scope]
          (! $scope.loaded false)
          (! $scope.init
             (fn [id]
               (when (and id (not= id ""))
                 (-> activityService
                     (.get id)
                     (.then (fn [activity]
                              (! $scope.loaded true)
                              (! $scope.activity activity)))))))))
   :link (fn [$scope element attrs]
           (let [id (.-id attrs)]
             (.init $scope id)))))

(def.directive jiksnu.streamsWidget []
  (obj))

(def.directive jiksnu.subscribersWidget
  []
  (obj
   :templateUrl "/templates/subscribers-widget"
   )
  )

(def.directive jiksnu.subscriptionsWidget
  []
  (obj
   :templateUrl "/templates/subscriptions-widget"
   )
  )
