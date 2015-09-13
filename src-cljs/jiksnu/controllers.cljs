(ns jiksnu.controllers
  (:require jiksnu.app
            jiksnu.models
            [jiksnu.helpers :as helpers]
            jiksnu.services
            [jiksnu.templates :as templates])
  (:use-macros [gyr.core :only [def.controller]]
               [jiksnu.macros :only [page-controller]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n]]))

(def.controller jiksnu.AdminActivitiesController
  [$scope $http]
  (! $scope.init (helpers/fetch-page $scope $http "/model/activities.json"))
  (.init $scope))

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

(def.controller jiksnu.FollowButtonController
  [$scope app Users]

  (! $scope.submit
     (fn []
       (.log js/console "Submit button pressed" app)
       )
     )

  )


(def.controller jiksnu.LeftColumnController
  [$scope $http]
  (! $scope.groups (clj->js helpers/nav-info)))


(def.controller jiksnu.ListFollowersController
  [$scope subpageService Users]
  (! $scope.formShown false)
  (! $scope.toggle (fn [] (! $scope.formShown (not (? $scope.formShown)))))
  (helpers/init-subpage $scope subpageService Users "followers"))

(def.controller jiksnu.ListFollowingController
  [$scope subpageService Users]
  (! $scope.formShown false)
  (! $scope.toggle (fn [] (! $scope.formShown (not (? $scope.formShown)))))
  (helpers/init-subpage $scope subpageService Users "following"))

(def.controller jiksnu.ListGroupsController
  [$scope subpageService Users]
  (! $scope.formShown false)
  (! $scope.toggle (fn [] (! $scope.formShown (not (? $scope.formShown)))))
  (helpers/init-subpage $scope subpageService Users "groups"))

(def.controller jiksnu.ListStreamsController
  [$scope subpageService Users]
  (! $scope.formShown false)
  (! $scope.toggle (fn [] (! $scope.formShown (not (? $scope.formShown)))))
  (! $scope.addStream (partial helpers/add-stream $scope))
  (helpers/init-subpage $scope subpageService Users "streams"))

(def.controller jiksnu.LoginPageController
  [$scope app]
  (! $scope.login (fn []
                    (let [username (.-username $scope)
                          password (.-password $scope)]
                      (.login app username password)))))

(def.controller jiksnu.LogoutController [])

(page-controller Activities    "activities"    [])
(page-controller Clients       "clients"       [])
(page-controller Conversations "conversations" ["activities"])
(page-controller Domains       "domains"       [])
(page-controller FeedSources   "feed-sources"  [])
(page-controller Groups        "groups"        [])
(page-controller Resources     "resources"     [])
(page-controller Streams       "streams"       [])
(page-controller Users         "users"         [])

(def.controller jiksnu.NavBarController
  [$scope app hotkeys $state]
  (helpers/setup-hotkeys hotkeys $state)
  (.$watch $scope (.-data app) (fn [d] (! $scope.app d)))
  (! $scope.app app)
  (! $scope.logout (.-logout app))
  (.fetchStatus app))

(def.controller jiksnu.NewPostController
  [$scope geolocation app pageService]
  (let [default-form (obj
                      :source "web"
                      :privacy "public"
                      :title ""
                      :content "")]
    (.$watch $scope #(? app.data) (fn [d] (! $scope.app d)))

    (.$watch $scope
             #(? $scope.form.shown)
             (fn [b]
               (when b
                 (-> (.getLocation geolocation)
                     (.then (fn [data]
                              (! $scope.activity.geo.latitude data.coords.latitude)
                              (! $scope.activity.geo.longitude data.coords.longitude)))))))

    (! $scope.toggle (fn [] (! $scope.form.shown (not $scope.form.shown))))
    (! $scope.reset  (fn [] (! $scope.activity default-form)))
    (! $scope.submit (fn []
                       (-> (.post app $scope.activity)
                           (.then (fn []
                                    (.reset $scope)
                                    (.toggle $scope)
                                    (.fetch pageService "activities"))))))
    (.reset $scope)))

(def.controller jiksnu.RegisterPageController
  [$http $scope]
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
  [$scope])

(def.controller jiksnu.ShowActivityController
  [$scope $stateParams Activities]
  (! $scope.loaded false)

  (! $scope.init
     (fn [id]
       (when (and id (not= id ""))
         (.bindOne Activities id $scope "activity")
         (-> (.find Activities id)
             (.then (fn [] (! $scope.loaded true)))))))

  (.init $scope (.-id $scope)))

(def.controller jiksnu.ShowDomainController
  [$scope $stateParams Domains]
  (! $scope.loaded false)
  (! $scope.init
     (fn [id]
       (.bindOne Domains id $scope "domain")
       (-> (.find Domains id)
           (.then (fn [] (! $scope.loaded true))))))
  (.init $scope (.-_id $stateParams)))

(def.controller jiksnu.ShowConversationController
  [$scope $stateParams Conversations]
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
  (! $scope.init (fn [stream] (! $scope.loaded true)))

  (if-let [stream (? $scope.stream)]
    (.init $scope stream)
    (if-let [id (? $scope.streamId)]
      (-> (.find Streams id)
          (.then (fn [stream]
                   (! $scope.stream stream)
                   (.init $scope stream))))
      (throw "No stream or stream id provided"))))

(def.controller jiksnu.ShowUserController
  [$scope $stateParams Users]
  (let [username (.-username $stateParams)
        domain (.-domain $stateParams)
        id (or (.-_id $stateParams)
               (str "acct:" username "@" domain))]
    (! $scope.init
       (fn [id]
         (! $scope.loaded false)
         (.bindOne Users id $scope "user")
         (-> (.find Users id)
             (.then (fn [user] (! $scope.loaded true))))))
    (.init $scope id)))
