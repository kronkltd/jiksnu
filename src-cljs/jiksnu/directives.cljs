(ns jiksnu.directives
  (:require jiksnu.app
            [jiksnu.helpers :refer [template-string]]
            [jiksnu.templates :as templates])
  (:use-macros [gyr.core :only [def.directive]]
               [purnam.core :only [! obj]]))

(def.directive jiksnu.showActivity
  [$http]
  (obj
   :templateUrl "/partials/show-activity.html"
   :link (fn [$scope element attrs]
           (let [id (.-id attrs)]
             (-> $http
                 (.get (str "/notice/" id ".json"))
                 (.success (fn [data]
                             (! $scope.activity data))))))))

(def.directive jiksnu.StreamsWidget []
  (obj))

(def.directive jiksnu.AddStreamForm []
  (obj))

(def.directive jiksnu.AddWatcherForm []
  (obj))

(def.directive jiksnu.jiksnuLeftColumn []
  (obj
    :template (template-string templates/left-column-section)
    :controller "LeftColumnController"))

(def.directive jiksnu.jiksnuNavBar []
  (obj
    :template (template-string templates/navbar-section)
    :controller "NavBarController"))

(def.directive jiksnu.jiksnuNewPost
  []
  (obj
   :template (template-string templates/add-post-form)
   :controller "NewPostController"
   )

  )

(def.directive jiksnu.jiksnuRightColumn []
  (obj
   :template (template-string templates/right-column-section)
   :controller "RightColumnController"))

