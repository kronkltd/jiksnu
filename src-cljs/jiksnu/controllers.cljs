(ns jiksnu.controllers
  (:require [jiksnu.templates :as templates]
            [hipo :as hipo :include-macros true]
            [purnam.native.functions :refer [js-map]])
  (:use-macros [gyr.core :only [def.module def.controller
                                def.value def.constant
                                def.filter def.factory
                                def.provider def.service
                                def.directive def.config]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n]]))

(defn with-template
  [o]
  (clj->js
   (merge
    {"navbar" {:template (hipo/create templates/navbar-section)
               :controller "NavBarController"}
     "newPost" {:templateUrl "/partials/new-post.html"
                :controller "NewPostController"}
     "leftColumn" {:template (hipo/create templates/left-column-section)
                   :controller "LeftColumnController"}
     "rightColumn" {:templateUrl "/partials/right-column.html"
                    :controller "RightColumnController"}}
    o)))

(defn add-states
  [$stateProvider data]
  (doseq [[state uri controller template] data]
    (.state $stateProvider
            (obj
             :name state
             :url uri
             :views
             (with-template
               {"" {:controller (str controller "Controller")
                    :template (hipo/create template)}})))))

(defn fetch-page
  [$scope $http url]
  (fn []
    (-> $http
        (.get url)
        (.success
         (fn [data]
           (! $scope.page data))))))


(def.module jiksnuApp [ui.router ui.bootstrap angularMoment
                       ui.bootstrap.tabs])

(def states
  [
   ["registerPage" "/main/register" "RegisterPage" templates/register-page]
   ["loginPage"    "/main/login"    "LoginPage"    templates/login-page]
   ]
  )






(def.controller jiksnuApp.AppController
  [$scope]
  (! $scope.phones
     (clj->js [{:name "Nexus Foo"
                :snippet "foo"}
               {:name "bar"
                :snippet "bar"}])))

(def.controller jiksnuApp.NavBarController
  [$scope $http]
  (-> $http
      (.get "/status")
      (.success (fn [data]
                  (! $scope.app data)))))

(def.controller jiksnuApp.LeftColumnController
  [$scope $http]

  (-> $http
      (.get "/nav.js")
      (.success (fn [data]
                  (! $scope.groups data)))))


(def.controller jiksnuApp.LoginPageController [])
(def.controller jiksnuApp.RegisterPageController [])
(def.controller jiksnuApp.RightColumnController [])
(def.controller jiksnuApp.NewPostController [])

(def.controller jiksnuApp.ShowActivityController
  [$scope $http $stateParams]
  (let [id (.-id $stateParams)
        url (str "/notice/" id ".json")]
    (.info js/console "Showing Activity")
    (! $scope.loaded false)
    (! $scope.init
       (fn [id]
         (-> $http
             (.get url)
             (.success
              (fn [data]
                (.info js/console "Data" data)
                (! $scope.activity data)
                (! $scope.loaded true))))))
    (.init $scope id)))

(def.controller jiksnuApp.ShowDomainController
  [$scope $http $stateParams]
  (let [id (.-id $stateParams)
        url (str "/main/domains/" id ".json")]
    (.info js/console "Showing Domain")
    (! $scope.loaded false)
    (! $scope.init
       (fn [id]
         (-> $http
             (.get url)
             (.success
              (fn [data]
                (.info js/console "Data" data)
                (! $scope.domain data)
                (! $scope.loaded true))))))
    (.init $scope id)))

(def.controller jiksnuApp.ShowUserController
  [$scope $http $stateParams]
  (let [id (.-id $stateParams)
        url (str "/users/" id ".json")]
    ;; (! $scope.user (? $scope.$parent.activity.actor))
    (! $scope.loaded false)
    (! $scope.init
       (fn [id]
         (-> $http
             (.get url)
             (.success
              (fn [data]
                (! $scope.user data))))))
    (.init $scope id)))

(def.controller jiksnuApp.ConversationListController
  [$scope $http]
  (.log js/console "Indexing conversations")
  (! $scope.init (fetch-page $scope $http "/main/conversations.json"))
  (.init $scope))





(def.controller jiksnuApp.IndexClientsController
  [$scope $http]
  (.info js/console "Indexing clients")
  (! $scope.init (fetch-page $scope $http "/main/clients.json"))
  (.init $scope))

(def.controller jiksnuApp.IndexDomainsController
  [$scope $http]
  (.info js/console "Indexing domains")
  (! $scope.init (fetch-page $scope $http "/main/domains.json"))
  (.init $scope))

(def.controller jiksnuApp.IndexGroupsController
  [$scope $http]
  (.info js/console "Indexing groups")
  (! $scope.init (fetch-page $scope $http "/main/groups.json"))
  (.init $scope))

(def.controller jiksnuApp.IndexResourcesController
  [$scope $http]
  (.info js/console "Indexing resources")
  (! $scope.init (fetch-page $scope $http "/resources.json"))
  (.init $scope))

(def.controller jiksnuApp.IndexUsersController
  [$scope $http]
  (.info js/console "Indexing users")
  (! $scope.init (fetch-page $scope $http "/users.json"))
  (.init $scope))

(def.controller jiksnuApp.AdminConversationsController
  [$scope $http]
  (! $scope.init (fetch-page $scope $http "/admin/conversations.json"))
  (.init $scope))





(def.directive jiksnuApp.showActivity
  [$http]
  (obj
   :templateUrl "/partials/show-activity.html"
   :link (fn [$scope element attrs]
           (let [id (.-id attrs)]
             (.log js/console "running link" id)

             (-> $http
                 (.get (str "/notice/" id ".json"))
                 (.success (fn [data]
                             (! $scope.activity data))))))))

(def.directive jiksnuApp.StreamsWidget []
  (obj)
  )
(def.directive jiksnuApp.AddStreamForm []
  (obj)
  )


(def.config jiksnuApp [$stateProvider $urlRouterProvider
                       $locationProvider]

  (.otherwise $urlRouterProvider "/")

  (-> $locationProvider
      (.hashPrefix "!")
      (.html5Mode true))

  (doto $stateProvider
    (add-states states)

    (.state "root"
            (obj
             :url "/"
             ;; :abstract true
             :views
             (with-template
               {"" {:templateUrl "/partials/public-timeline.html"
                    :controller "ConversationListController"}})))

    (.state "indexDomains"
            (obj
             :url "/main/domains"
             :views
             (with-template
               {"" {:templateUrl "/partials/index-domains.html"
                    :controller "IndexDomainsController"}})))

    (.state "indexGroups"
            (obj
             :url "/main/groups"
             :views
             (with-template
               {"" {:templateUrl "/partials/index-groups.html"
                    :controller "IndexGroupsController"}})))

    (.state "indexResources"
            (obj
             :url "/resources"
             :views
             (with-template
               {"" {:templateUrl "/partials/index-resources.html"
                    :controller "IndexResourcesController"}})))

    (.state "indexUsers"
            (obj
             :url "/users"
             :views
             (with-template
               {"" {:templateUrl "/partials/index-users.html"
                    :controller "IndexUsersController"}})))

    (.state "showActivity"
            (obj
             :url "/notice/:id"
             :views
             (with-template
               {"" {:templateUrl "/partials/show-activity.html"
                    :controller "ShowActivityController"}})))

    (.state "showDomain"
            (obj
             :url "/main/domains/:id"
             :views
             (with-template
               {"" {:templateUrl "/partials/show-domain.html"
                    :controller "ShowDomainController"}})))

    (.state "showUser"
            (obj
             :url "/users/:id"
             :views
             (with-template
               {"" {:templateUrl "/partials/show-user.html"
                    :controller "ShowUserController"}})))

    (.state "adminConversations"
            (obj
             :url "/admin/conversations"
             :views
             (with-template
               {"" {:templateUrl "/partials/admin-conversations.html"
                    :controller "AdminConversationsController"}})))

    ))

