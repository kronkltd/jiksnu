(ns jiksnu.controllers
  (:require jiksnu.app
            [jiksnu.helpers :as helpers]
            jiksnu.services
            [jiksnu.templates :as templates]
            [purnam.native.functions :refer [js-map]])
  (:use-macros [gyr.core :only [def.controller]]
               [jiksnu.macros :only [page-controller]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n]]))

(defn init-page
  [$scope pageService page-type]
  (! $scope.loaded false)
  (! $scope.init
     (fn []
       (-> pageService
           (.fetch page-type)
           (.then (fn [page]
                    (! $scope.page page)
                    (! $scope.loaded true)))))))

(def.controller jiksnu.AdminActivitiesController [])

(def.controller jiksnu.AdminConversationsController
  [$scope $http]
  (! $scope.init (helpers/fetch-page $scope $http "/admin/conversations.json"))
  (.init $scope))

(def.controller jiksnu.AdminGroupsController [])


(def.controller jiksnu.AdminUsersController
  [$scope $http]
  (! $scope.init (helpers/fetch-page $scope $http "/admin/users.json"))
  (.init $scope))



(def.controller jiksnu.AppController [])
(def.controller jiksnu.AvatarPageController [])

(def.controller jiksnu.LeftColumnController
  [$scope $http]
  (! $scope.groups (clj->js helpers/nav-info)))


(def.controller jiksnu.LoginPageController [])

(def.controller jiksnu.LogoutController
  [$scope $http app]
  (-> $http
      (.post "/main/logout")
      (.success (fn [data]
                  (.fetchStatus app)))))

(page-controller Activities    "activities")
(page-controller Clients       "clients")
(page-controller Conversations "conversations")
(page-controller Domains       "domains")
(page-controller FeedSources   "feed-sources")
(page-controller Groups        "groups")
(page-controller Resources     "resources")
(page-controller Users         "users")

(def.controller jiksnu.NavBarController
  [$scope app]
  (! $scope.app app.data)
  (.fetchStatus app))

(def.controller jiksnu.NewPostController
  [$scope $http $rootScope geolocation]

  (! $scope.reset
     (fn []
       (! $scope.activity
          (obj
           :source "web"
           :privacy "public"
           :title ""
           :content ""))

       (-> (.getLocation geolocation)
           (.then (fn [data]
                    (! $scope.activity.geo.latitude data.coords.latitude)
                    (! $scope.activity.geo.longitude data.coords.longitude))))))

  (! $scope.submit
     (fn []
       (-> $http
           (.post "/notice/new" $scope.activity)
           (.success
            (fn [data]
              (.$broadcast $rootScope "updateConversations"))))))

  (.reset $scope))

(def.controller jiksnu.RegisterPageController [])

(def.controller jiksnu.RightColumnController
  [$scope app]
  (! $scope.app app.data)
  )

(def.controller jiksnu.SettingsPageController [])

(def.controller jiksnu.ShowActivityController
  [$scope $http $stateParams activityService]
  (! $scope.loaded false)
  (! $scope.init
     (fn [id]
       (when (and id (not= id ""))
         (-> activityService
             (.get id)
             (.then (fn [activity]
                      (! $scope.loaded true)
                      (! $scope.activity activity)))))))
  (.init $scope (.-id $stateParams)))

(def.controller jiksnu.ShowDomainController
  [$scope $http $stateParams]
  (! $scope.loaded false)
  (! $scope.init
     (fn [id]
       (let [url (str "/main/domains/" id ".json")]
         (-> $http
             (.get url)
             (.success
              (fn [data]
                (! $scope.domain data)
                (! $scope.loaded true)))))))
  (.init $scope (.-id $stateParams)))

(def.controller jiksnu.ShowUserController
  [$scope $http $stateParams userService]
  (! $scope.loaded false)
  (! $scope.init
     (fn [id]
       (-> userService
           (.get id)
           (.then (fn [user] (! $scope.user user))))))
  (.init $scope (.-id $stateParams)))
