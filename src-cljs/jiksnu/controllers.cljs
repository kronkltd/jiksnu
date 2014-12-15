(ns jiksnu.controllers
  (:require [purnam.native.functions :refer [js-map]])
  (:use-macros [gyr.core :only [def.module def.controller
                                def.value def.constant
                                def.filter def.factory
                                def.provider def.service
                                def.directive def.config]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n]]))

(def.module jiksnuApp [ngRoute
                       ;; ui
                       ui.router
                       ])

(def.config jiksnuApp [
                       $stateProvider $urlRouterProvider
                       ;; $locationProvider $routeProvider
                       ]

  (.otherwise $urlRouterProvider "/")

  (doto $stateProvider

    (.state "home"
            (obj
             :url ""
             :templateUrl "/partials/public-timeline.html"
             :controller "ConversationListController"))

    (.state "showActivity"
            (obj
             :url "/notice/:id"
             :templateUrl "/partials/show-activity.html"
             :controller "ShowActivityController"))

    (.state "indexUsers"
            (obj
             :url "/users"
             :templateUrl "/partials/index-users.html"
             :controller "UserIndexController"))))

(def.controller jiksnuApp.JiksnuController
  [$scope]
  (! $scope.phones
     (clj->js [{:name "Nexus Foo"
                :snippet "foo"}
               {:name "bar"
                :snippet "bar"}])))

(def.controller jiksnuApp.NavController
  [$scope]
  (let [items
        [["/"                  "Public"]
         ["/users"             "Users"]
         ["/main/feed-sources" "Feeds"]
         ["/main/domains"      "Domains"]
         ["/main/groups"       "Groups"]
         ["/resources"         "Resources"]]
        items (js-map
               (fn [line]
                 (obj
                  :label (nth line 1)
                  :href (nth line 0)))
               items)
        ]
    (.log js/console "items" items)
    (! $scope.items items)))

(def.controller jiksnuApp.NavBarController
  [$scope]

  ;; TODO: pull from app config on page
  (! $scope.app.name "Jiksnu"))

(def.controller jiksnuApp.ConversationListController
  [$scope $http]

  (.log js/console "Indexing conversations")

  (! $scope.init
     (fn []
       (-> $http
           (.get "/main/conversations.json")
           (.success
            (fn [data]
              (.info js/console "Data" data)
              (! $scope.page data))))))

  (.init $scope))

(def.controller jiksnuApp.ShowActivityController
  [$scope $http]

  (.info js/console "Showing Activity")
  (! $scope.loaded false)

  (! $scope.init
     (fn [id]
       (-> $http
           (.get  (str "/notice/" id ".json"))
           (.success
            (fn [data]
              (.info js/console "Data" data)
              (! $scope.activity data)
              (! $scope.loaded true)
              )))))

  (.init $scope "53967919b7609432045de504"))

(def.controller jiksnuApp.ShowUserController
  [$scope $http $attrs]

  ;; (.log js/console (? $scope.$parent.activity))

  (! $scope.user (? $scope.$parent.activity.actor))
  ;; (.log js/console (? $scope.$parent.loaded))

  (! $scope.init
     (fn [id]
       (-> $http
           (.get (str "/main/users/" id ".json"))
           (.success
            (fn [data]
              (! $scope.user data))))))

  #_(.init $scope ""))

(def.controller jiksnuApp.UserIndexController
  [$scope $http]

  (.info js/console "Indexing users")

  (! $scope.init
     (fn []
       (-> $http
           (.get "/users.json")
           (.success
            (fn [data]
              (! $scope.page data))))))

  (.init $scope))
