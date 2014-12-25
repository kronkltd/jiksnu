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


(def nav-info
  [{:label "Home"
    :items
    [{:title "Public"
      ;; :href "/"
      :state "home"}
     {:title "Users"
      ;; :href "/users"
      :state "indexUsers"}
     ;; {:title "Feeds"
     ;;  :href "/main/feed-sources"
     ;;  :state "indexFeedSources"}
     {:title "Domains"
      ;; :href "/main/domains"
      :state "indexDomains"}
     {:title "Groups"
      ;; :href "/main/groups"
      :state "indexGroups"}
     {:title "Resources"
      ;; :href "/resources"
      :state "indexResources"}]}
   #_{:label "Settings"
    :items
    [{:href "/admin/settings"
      :title "Settings"}]}
   #_(when (session/is-admin?)
     {:label "Admin"
      :items
      [{:href "/admin/activities"
        :title "Activities"}
       {:href "/admin/auth"
        :title "Auth"}
       {:href "/admin/clients"
        :title "Clients"}
       {:href "/admin/conversations"
        :title "Conversations"
        :state "adminConversations"}
       {:href "/admin/feed-sources"
        :title "Feed Sources"}
       {:href "/admin/feed-subscriptions"
        :title "Feed Subscriptions"}
       {:href "/admin/groups"
        :title "Groups"}
       {:href "/admin/group-memberships"
        :title "Group Memberships"}
       {:href "/admin/keys"
        :title "Keys"}
       {:href "/admin/likes"
        :title "Likes"}
       {:href "/admin/request-tokens"
        :title "Request Tokens"}
       {:href "/admin/streams"
        :title "Streams"}
       {:href "/admin/subscriptions"
        :title "Subscriptions"}
       {:href "/admin/users"
        :title "Users"}
       {:href "/admin/workers"
        :title "Workers"}]})]
  )

(def states
  [
   ["avatarPage"   "/main/avatar"   "AvatarPage"   templates/avatar-page]
   ["registerPage" "/main/register" "RegisterPage" templates/register-page]
   ["loginPage"    "/main/login"    "LoginPage"    templates/login-page]
   ]
  )

(def templated-states
  [
   {:name "root"
    :url ""
    :abstract true
    }
   {:name "root.home"
    :parent "root"
    :url "/"
    ;; :abstract true
    :views {:templateUrl "/partials/public-timeline.html"
            :controller "ConversationListController"}}
   {:name "indexDomains"
    :url "/main/domains"
    :views {:templateUrl "/partials/index-domains.html"
            :controller "IndexDomainsController"}}
   {:name  "indexGroups"
    :url "/main/groups"
    :views {:templateUrl "/partials/index-groups.html"
            :controller "IndexGroupsController"}}
   {:name "indexResources"
    :url "/resources"
    :views {:templateUrl "/partials/index-resources.html"
            :controller "IndexResourcesController"}}
   {:name  "indexUsers"
    :url "/users"
    :views {:templateUrl "/partials/index-users.html"
            :controller "IndexUsersController"}}
   ;; {:name  "showActivity"
   ;;  :url "/notice/:id"
   ;;  :views {:templateUrl "/partials/show-activity.html"
   ;;          :controller "ShowActivityController"}}
   ;; {:name  "showDomain"
   ;;  :url "/main/domains/:id"
   ;;  :views {:templateUrl "/partials/show-domain.html"
   ;;          :controller "ShowDomainController"}}
   ;; {:name  "showUser"
   ;;  :url "/users/:id"
   ;;  :views {:templateUrl "/partials/show-user.html"
   ;;          :controller "ShowUserController"}}
   ;; {:name  "adminConversations"
   ;;  :url "/admin/conversations"
   ;;  :views {:templateUrl "/partials/admin-conversations.html"
   ;;          :controller "AdminConversationsController"}}

   ])


(def.config jiksnuApp [$stateProvider $urlRouterProvider $locationProvider
                       ;; $rootScope
                       ;; $compileProvider
                       ]

  ;; (.on $rootScope "$stateChangeStart"
  ;;      (fn [e to]
  ;;        (.log js/console "to:" to)))


  ;; (.otherwise $urlRouterProvider "/")
  (-> $locationProvider
      (.hashPrefix "!")
      (.html5Mode true))
  (doseq [state templated-states]
    (let [views (:views state)]
      (.state $stateProvider
              (clj->js (assoc state :views (with-template
                                             {"" views}))))))
  (add-states $stateProvider states))





(def.controller jiksnuApp.AppController [])
(def.controller jiksnuApp.AvatarPageController [])
(def.controller jiksnuApp.LoginPageController [])
(def.controller jiksnuApp.RegisterPageController [])
(def.controller jiksnuApp.RightColumnController [])
(def.controller jiksnuApp.NewPostController [])


(def.controller jiksnuApp.NavBarController
  [$scope $http]
  (-> $http
      (.get "/status")
      (.success (fn [data]
                  (! $scope.app data)))))

(def.controller jiksnuApp.LeftColumnController
  [$scope $http]
  (.info js/console "fetching nav")

  ;; (-> $http
  ;;     (.get "/nav.js")
  ;;     (.success (fn [data]
  ;;                 (.info js/console "fetched nav")
  ;;                 (! $scope.groups data))))

  (! $scope.groups (clj->js nav-info))
  )


(def.controller jiksnuApp.ShowActivityController
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

(def.controller jiksnuApp.ShowDomainController
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

(def.controller jiksnuApp.ShowUserController
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
             (-> $http
                 (.get (str "/notice/" id ".json"))
                 (.success (fn [data]
                             (! $scope.activity data))))))))

(def.directive jiksnuApp.StreamsWidget []
  (obj))

(def.directive jiksnuApp.AddStreamForm []
  (obj))

(def.directive jiksnuApp.AddWatcherForm []
  (obj))
