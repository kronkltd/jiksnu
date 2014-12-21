(ns jiksnu.controllers
  (:require [purnam.native.functions :refer [js-map]])
  (:use-macros [gyr.core :only [def.module def.controller
                                def.value def.constant
                                def.filter def.factory
                                def.provider def.service
                                def.directive def.config]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n]]))

(def.module jiksnuApp [ui.router ui.bootstrap])


(def.controller jiksnuApp.AppController
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
    #_(! $scope.items items)))

(def.controller jiksnuApp.NavBarController
  [$scope]

  ;; TODO: pull from app config on page
  (! $scope.app.name "Jiksnu"))

(def.controller jiksnuApp.LeftColumnController
  [$scope]
  (! $scope.loaded false)
  )

(def.controller jiksnuApp.RightColumnController
  [$scope]
  (! $scope.loaded false)
  )

(def.controller jiksnuApp.NewPostController
  [$scope]
  (! $scope.loaded false)
  )
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

(defn fetch-page
  [$scope $http url]
  (fn []
    (-> $http
        (.get url)
        (.success
         (fn [data]
           (! $scope.page data))))))

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

(defn with-template
  [o]
  (clj->js
   (merge
    {"leftColumn" {:templateUrl "/partials/left-column.html"
                   :controller "LeftColumnController"}
     "rightColumn" {:templateUrl "/partials/right-column.html"
                    :controller "RightColumnController"}}
    o)))

(def.config jiksnuApp [$stateProvider $urlRouterProvider
                       $locationProvider]

  (.otherwise $urlRouterProvider "/")

  (-> $locationProvider
      (.hashPrefix "!")
      (.html5Mode true))

  (doto $stateProvider

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

