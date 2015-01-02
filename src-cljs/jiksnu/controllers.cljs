(ns jiksnu.controllers
  (:require jiksnu.app
            [jiksnu.helpers :as helpers]
            jiksnu.services
            [jiksnu.templates :as templates]
            [purnam.native.functions :refer [js-map]])
  (:use-macros [gyr.core :only [def.module def.controller
                                def.value def.constant
                                def.filter def.factory
                                def.provider def.service
                                def.directive def.config]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n]]))

(def.controller jiksnu.AdminConversationsController
  [$scope $http]
  (! $scope.init (helpers/fetch-page $scope $http "/admin/conversations.json"))
  (.init $scope))

(def.controller jiksnu.AdminUsersController
  [$scope $http]
  (! $scope.init (helpers/fetch-page $scope $http "/admin/users.json"))
  (.init $scope))



(def.controller jiksnu.AppController [])
(def.controller jiksnu.AvatarPageController [])

(def.controller jiksnu.LeftColumnController
  [$scope $http]
  (.info js/console "fetching nav")
  (! $scope.groups (clj->js helpers/nav-info)))


(def.controller jiksnu.LoginPageController [])

(def.controller jiksnu.LogoutController
  [$scope $http app]

  (-> $http
      (.post "/main/logout")
      (.success (fn [data]
                  (.log js/console "data:" data)
                  (.fetchStatus app)
                  ;; TODO: refresh status

                  ))
      )

  )

(def.controller jiksnu.IndexClientsController
  [$scope $http]
  (.info js/console "Indexing clients")
  (! $scope.init (helpers/fetch-page $scope $http "/main/clients.json"))
  (.init $scope))

(def.controller jiksnu.IndexConversationsController
  [$scope $http notify]
  (.log js/console "Indexing conversations")
  (notify "update conversations")
  (! $scope.init (helpers/fetch-page $scope $http "/main/conversations.json"))
  (.$on $scope
       "updateConversations"
       (fn [e]
         (.log js/console "updateConversations")
         (notify "update conversations")
         (.init $scope)
         )
       )
  (.init $scope))

(def.controller jiksnu.IndexDomainsController
  [$scope $http]
  (.info js/console "Indexing domains")
  (! $scope.init (helpers/fetch-page $scope $http "/main/domains.json"))
  (.init $scope))

(def.controller jiksnu.IndexFeedSourcesController
  [$scope $http]
  (.info js/console "Indexing feed sources")
  (! $scope.init (helpers/fetch-page $scope $http "/main/feed-sources.json"))
  (.init $scope))

(def.controller jiksnu.IndexGroupsController
  [$scope $http]
  (.info js/console "Indexing groups")
  (! $scope.init (helpers/fetch-page $scope $http "/main/groups.json"))
  (.init $scope))

(def.controller jiksnu.IndexResourcesController
  [$scope $http]
  (.info js/console "Indexing resources")
  (! $scope.init (helpers/fetch-page $scope $http "/resources.json"))
  (.init $scope))

(def.controller jiksnu.IndexUsersController
  [$scope $http]
  (.info js/console "Indexing users")
  (! $scope.init (helpers/fetch-page $scope $http "/users.json"))
  (.init $scope))

(def.controller jiksnu.NavBarController
  [$scope app]
  (.log js/console "app:" app)
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
       (.log js/console "submitting")
       (-> $http
           (.post "/notice/new" $scope.activity)
           (.success
            (fn [data]
              (.log js/console "Response " data)
              (.$broadcast $rootScope "updateConversations"))))))

  (.reset $scope))

(def.controller jiksnu.RegisterPageController [])

(def.controller jiksnu.RightColumnController
  [$scope app]
  (! $scope.app app.data)
  )

(def.controller jiksnu.ShowActivityController
  [$scope $http $stateParams]
  (! $scope.loaded false)
  (! $scope.init
     (fn [id]
       (.info js/console "Showing Activity")
       (let [url (str "/notice/" id ".json")]
         (-> $http
             (.get url)
             (.success
              (fn [data]
                (.info js/console "Data" data)
                (! $scope.activity data)
                (! $scope.loaded true)))))))
  (.init $scope (.-id $stateParams)))

(def.controller jiksnu.ShowDomainController
  [$scope $http $stateParams]
  (! $scope.loaded false)
  (! $scope.init
     (fn [id]
       (.info js/console "Showing Domain")
       (let [url (str "/main/domains/" id ".json")]
         (-> $http
             (.get url)
             (.success
              (fn [data]
                (.info js/console "Data" data)
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
