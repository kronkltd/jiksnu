(ns jiksnu.controllers
  (:require jiksnu.app
            [jiksnu.helpers :as helpers]
            [jiksnu.templates :as templates]
            [hipo :as hipo :include-macros true]
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



(def.controller jiksnu.AppController [])
(def.controller jiksnu.AvatarPageController [])

(def.controller jiksnu.LeftColumnController
  [$scope $http]
  (.info js/console "fetching nav")
  (! $scope.groups (clj->js helpers/nav-info)))


(def.controller jiksnu.LoginPageController [])

(def.controller jiksnu.LogoutController
  [$scope $http]

  (-> $http
      (.post "/main/logout")
      (.success (fn [data]
                  (.log js/console "data:" data)

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
  [$scope $http]
  (.log js/console "Indexing conversations")
  (! $scope.init (helpers/fetch-page $scope $http "/main/conversations.json"))
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
  [$scope $http]
  (-> $http
      (.get "/status")
      (.success (fn [data]
                  (! $scope.app data)))))

(def.controller jiksnu.NewPostController
  [$scope]
  (! $scope.activity.source "web")
  (! $scope.activity.privacy "public"))




(def.controller jiksnu.RegisterPageController [])
(def.controller jiksnu.RightColumnController [])



(def.controller jiksnu.ShowActivityController
  [$scope $http $stateParams]
  (let [id (.-id $stateParams)]
    (.info js/console "Showing Activity")
    (! $scope.loaded false)
    (! $scope.init
       (fn [id]
         (let [url (str "/notice/" id ".json")]
           (-> $http
               (.get url)
               (.success
                (fn [data]
                  (.info js/console "Data" data)
                  (! $scope.activity data)
                  (! $scope.loaded true)))))))
    (.init $scope id)))

(def.controller jiksnu.ShowDomainController
  [$scope $http $stateParams]
  (let [id (.-id $stateParams)]
    (.info js/console "Showing Domain")
    (! $scope.loaded false)
    (! $scope.init
       (fn [id]
         (let [url (str "/main/domains/" id ".json")]
           (-> $http
               (.get url)
               (.success
                (fn [data]
                  (.info js/console "Data" data)
                  (! $scope.domain data)
                  (! $scope.loaded true)))))))
    (.init $scope id)))

(def.controller jiksnu.ShowUserController
  [$scope $http $stateParams]
  (let [id (.-id $stateParams)]
    (! $scope.loaded false)
    (! $scope.init
       (fn [id]
         (let [url (str "/users/" id ".json")]
           (-> $http
               (.get url)
               (.success
                (fn [data]
                  (! $scope.user data)))))))
    (.init $scope id)))














