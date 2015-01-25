(ns jiksnu.controllers
  (:require jiksnu.app
            jiksnu.factories
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

(def.controller jiksnu.DisplayAvatarController
  [$scope Users]
  (! $scope.init
     (fn [id]
       (when (and id (not= id ""))
         (! $scope.size 32)
         (.bindOne Users $scope "user" id)
         (.find Users id)))))

(def.controller jiksnu.FollowersListController
  [$scope])

(def.controller jiksnu.FollowingListController
  [$scope $http]



)

(def.controller jiksnu.LeftColumnController
  [$scope $http]
  (! $scope.groups (clj->js helpers/nav-info)))


(def.controller jiksnu.LoginPageController
  [$scope app]
  (! $scope.login (fn []
                    (let [username (.-username $scope)
                          password (.-password $scope)]
                      (.log js/console "login"
                            username
                            password)
                      (.login app username password)))))

(def.controller jiksnu.LogoutController [])

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
  (.$watch $scope #(? app.data) (fn [d] (! $scope.app d)))
  (! $scope.app2 app)
  (! $scope.logout
     (fn []
       (.log js/console "logging out")
       (.logout app)))
  (.fetchStatus app))

(def.controller jiksnu.NewPostController
  [$scope $http $rootScope geolocation app]

  (.$watch $scope #(? app.data) (fn [d] (! $scope.app d)))

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
  (.$watch $scope #(? app.data) (fn [d] (! $scope.app d)))
  (! $scope.foo "bar")
  )

(def.controller jiksnu.SettingsPageController [])

(def.controller jiksnu.SubscribersWidgetController
  [$scope app]

)

(def.controller jiksnu.ShowActivityController
  [$scope $http $stateParams Activities]
  (! $scope.loaded false)
  (! $scope.init
     (fn [id]
       (when (and id (not= id ""))
         (.bindOne Activities $scope "activity" id)
         (.find Activities id))))
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
  [$scope $http $stateParams Users]
  (let [username (.-username $stateParams)
        domain (.-domain $stateParams)
        id (str "acct:" username "@" domain)]
    (! $scope.loaded false)
    (! $scope.init
       (fn [id]
         (when (and id (not= id ""))
           (.bindOne Users $scope "user" id)
           (.find Users id))))
    (.init $scope id)))
