(ns jiksnu.directives
  (:require jiksnu.app
            [jiksnu.helpers :refer [template-string]]
            [jiksnu.templates :as templates])
  (:use-macros [gyr.core :only [def.directive]]
               [purnam.core :only [! arr obj]]))

(def.directive jiksnu.jiksnuShowActivity
  [$http]
  (obj
   :templateUrl "/templates/show-activity"
   :scope (obj)
   :controller
   (arr "$scope"
        (fn [$scope]
          (! $scope.init
             (fn [id]
               (-> $http
                   (.get (str "/notice/" id ".json"))
                   (.success (fn [data]
                               (! $scope.activity data))))))))
   :link (fn [$scope element attrs]
           (let [id (.-id attrs)]
             (.init $scope id)))))

(def.directive jiksnu.StreamsWidget []
  (obj))

(def.directive jiksnu.AddStreamForm []
  (obj))

(def.directive jiksnu.AddWatcherForm []
  (obj))

(def.directive jiksnu.jiksnuDisplayAvatar []
  (obj
   :templateUrl "/templates/display-avatar"))

(def.directive jiksnu.jiksnuLeftColumn []
  (obj
   :templateUrl "/templates/left-column-section"
   :scope true
   :controller "LeftColumnController"))

(def.directive jiksnu.jiksnuNavBar []
  (obj
   :templateUrl "/templates/navbar-section"
   :scope true
   :controller "NavBarController"))

(def.directive jiksnu.jiksnuNewPost
  []
  (obj
   :templateUrl "/templates/add-post-form"
   :scope true
   :controller "NewPostController"))

(def.directive jiksnu.jiksnuRightColumn []
  (obj
   :templateUrl "/templates/right-column-section"
   :scope true
   :controller "RightColumnController"))

