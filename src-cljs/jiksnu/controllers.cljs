(ns jiksnu.controllers
  (:require jiksnu.app
            jiksnu.factories
            [jiksnu.helpers :as helpers]
            jiksnu.services
            [jiksnu.templates :as templates])
  (:use-macros [gyr.core :only [def.controller]]
               [jiksnu.macros :only [page-controller]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n]]))

(defn fetch-sub-page
  [item subpageService subpage]
  (.log js/console "Fetching subpage:" item subpage)
  (-> subpageService
      (.fetch item subpage)
      (.then (fn [response] (aset item subpage (? response.body))))))

(defn init-page
  [$scope $rootScope pageService subpageService page-type subpages]
  (.$on $rootScope "updateCollection"
       (fn []
         (.init $scope)
         )
       )

  (! $scope.loaded false)
  (! $scope.init
     (fn []
       (-> pageService
           (.fetch page-type)
           (.then (fn [page]
                    (! $scope.page page)
                    (! $scope.loaded true)
                    (doall (map
                      (fn [item]
                        (doall (map (partial fetch-sub-page item subpageService)
                                    subpages))
                        )
                      (?
                       page.items)))
                    ))))))

(def.controller jiksnu.AdminActivitiesController
  [$scope $http]
  (! $scope.init (helpers/fetch-page $scope $http "/model/activities.json"))
  (.init $scope)

)

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
         (.bindOne Users id $scope "user")
         (.find Users id)))))

(def.controller jiksnu.FollowersListController
  [$scope subpageService Users]

  (! $scope.init
     (fn [id]
       (if (and id (not= id ""))
         (-> Users
             (.find id)
             (.then
              (fn [user]
                (-> user
                    (.getFollowers)
                    (.then (fn [page]
                             (! $scope.page page)))))))))))

(def.controller jiksnu.FollowingListController
  [$scope $http subpageService Users]

  (! $scope.init
     (fn [id]
       (if (and id (not= id ""))
         (-> Users
             (.find id)
             (.then
              (fn [user]
                (-> user
                    (.getFollowing)
                    (.then (fn [page]
                             ;; (.log js/console "following resolved" page)
                             (! $scope.page page)))))))))))

(def.controller jiksnu.GroupsListController
  [$scope subpageService Users]

  (! $scope.init
     (fn [id]
       (if (and id (not= id ""))
         (-> Users
             (.find id)
             (.then (fn [user]
                      (-> user
                          (.getGroups)
                          (.then (fn [page]
                                   (! $scope.page page))))))))))
)

(def.controller jiksnu.LeftColumnController
  [$scope $http]
  (! $scope.groups (clj->js helpers/nav-info)))


(def.controller jiksnu.LoginPageController
  [$scope app]
  (! $scope.login (fn []
                    (let [username (.-username $scope)
                          password (.-password $scope)]
                      (.login app username password)))))

(def.controller jiksnu.LogoutController [])

(page-controller Activities    "activities" [])
(page-controller Clients       "clients" [])
(page-controller Conversations "conversations" ["activities"])
(page-controller Domains       "domains" [])
(page-controller FeedSources   "feed-sources" [])
(page-controller Groups        "groups" [])
(page-controller Resources     "resources" [])
(page-controller Streams       "streams" [])
(page-controller Users         "users" [])

(def.controller jiksnu.NavBarController
  [$scope app hotkeys $state]

  (.add hotkeys (obj
                 :combo "g h"
                 :description "go home"
                 :callback (fn []
                             (.go $state "home"))))

  (.$watch $scope #(? app.data) (fn [d] (! $scope.app d)))
  (! $scope.app2 app)
  (! $scope.logout
     (fn []
       (.log js/console "logging out")
       (.logout app)))
  (.fetchStatus app))

(def.controller jiksnu.NewPostController
  [$scope $http $rootScope geolocation app pageService]

  (.$watch $scope #(? app.data) (fn [d] (! $scope.app d)))

  (.$watch $scope
          #(? $scope.form.shown)
          (fn [b]
            (.log js/console "b" b)
            (when b
              (-> (.getLocation geolocation)
                  (.then (fn [data]
                           (! $scope.activity.geo.latitude
                              data.coords.latitude)
                           (! $scope.activity.geo.longitude
                              data.coords.longitude)))))))

  (! $scope.toggle (fn []
                     (! $scope.form.shown (not $scope.form.shown))))

  (! $scope.reset
     (fn []
       (! $scope.activity
          (obj
           :source "web"
           :privacy "public"
           :title ""
           :content ""))))

  (! $scope.submit
     (fn []
       (-> $http
           (.post "/model/activities" $scope.activity)
           (.success
            (fn [data]
              (.$broadcast $rootScope "updateConversations"))))))

  (.reset $scope))

(def.controller jiksnu.RegisterPageController [$http $scope]
  (! $scope.register
     (fn []
       (.log js/console "Registering" (? $scope.reg))
       (.post $http "/register" $scope))))

(def.controller jiksnu.RightColumnController
  [$scope app]
  (.$watch $scope #(? app.data) (fn [d] (! $scope.app d)))
  (! $scope.foo "bar"))

(def.controller jiksnu.SettingsPageController [])

(def.controller jiksnu.SubscribersWidgetController
  [$scope app])

(def.controller jiksnu.ShowActivityController
  [$scope $http $stateParams Activities]
  (! $scope.loaded false)

  (! $scope.init
     (fn [id]
       (when (and id (not= id ""))
         (.bindOne Activities id $scope "activity")
         (.find Activities id))))
  (.init $scope (.-id $stateParams)))

(def.controller jiksnu.ShowDomainController
  [$scope $http $stateParams]
  (! $scope.loaded false)
  (! $scope.init
     (fn [id]
       (let [url (str "/model/domains/" id)]
         (-> $http
             (.get url)
             (.success
              (fn [data]
                (! $scope.domain data)
                (! $scope.loaded true)))))))
  (.init $scope (.-_id $stateParams)))

(def.controller jiksnu.ShowConversationController
  [$scope $http $stateParams Conversations]
  (! $scope.loaded false)
  (! $scope.init
     (fn [id]
       (.log js/console "id" id)
       (when (and id (not= id ""))
         (.bindOne Conversations id $scope "conversation")
         (let [d (.find Conversations id)]
           (.then d (fn [conversation]
                      (.log js/console )
                      (-> conversation
                          .getActivities
                          (.then  (fn [response]
                                    (.log js/console "Activities" response)
                                    (! $scope.activities
                                       (? response.body)))))))
           d))))
  (.init $scope (.-_id $stateParams)))

(def.controller jiksnu.ShowGroupController
  [$scope $http $stateParams]
  (! $scope.loaded false)
  (! $scope.addAdmin (fn [& opts]
                      (.log js/console opts)))
  (! $scope.addMember (fn [& opts]
                      (.log js/console opts)))
  (! $scope.init
     (fn [id]
       (let [url (str "/model/groups/" id)]
         (-> $http
             (.get url)
             (.success
              (fn [data]
                (! $scope.group data)
                (! $scope.loaded true)))))))
  (.init $scope (.-_id $stateParams)))

(def.controller jiksnu.ShowStreamMinimalController
  [$scope Streams]
  (! $scope.loaded false)
  (! $scope.init (fn [stream]
                   (.log js/console "init minimal show stream" stream)
                   (! $scope.loaded true)))

  (if-let [stream (? $scope.stream)]
    (.init $scope stream)
    (if-let [id (? $scope.streamId)]
      (-> (.find Streams id)
          (.then (fn [stream]
                   (! $scope.stream stream)
                   (.init $scope stream))))
      (throw "No stream or stream id provided"))))

(def.controller jiksnu.ShowUserController
  [$scope $http $stateParams Users]
  (let [username (.-username $stateParams)
        domain (.-domain $stateParams)
        id (or (.-_id $stateParams)
               (str "acct:" username "@" domain))]
    (! $scope.loaded false)
    (! $scope.init
       (fn [id]
         (.log js/console "Showing user: " id)
         (when (and id (not= id ""))
           (! $scope.loaded false)
           (.bindOne Users id $scope "user")
           (-> (.find Users id)
               (.then (fn [user] (! $scope.loaded true)))))))
    (.init $scope id)))

(defn add-stream
  [user stream-name]
  (.log js/console "Adding Stream: " stream-name))

(def.controller jiksnu.StreamListController
  [$scope Users subpageService]
  (! $scope.formShown false)
  (! $scope.addStream
     (fn []
       (.log js/console "Adding Stream" $scope)
       (add-stream (? $scope.user) (? $scope.stream.name))))

  (! $scope.fetchStreams
     (fn [user]
       (! $scope.stream.userId (.-_id user))
       (-> subpageService
           (.fetch user "streams")
           (.then (fn [page]
                    ;; (.log js/console page)
                    (aset user "streams" page))))))

  (! $scope.init
     (fn [id]
       (.log js/console "Init Stream list" id)
       (when (and id (not= id ""))
         (.then (.find Users id)
                (fn [user]
                  (.fetchStreams $scope user))))))

  (if-let [user (.-user $scope)]
    (.fetchStreams $scope user)
    (if-let [user-id (.-userId $scope)]
      (.init $scope user-id)
      (.error js/console "Couldn't determine user id"))))
