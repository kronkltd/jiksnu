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
       ;; (js/console.debug "Displaying avatar for " id)
       (when (and id (not= id ""))
         (! $scope.size 32)
         ;; (js/console.info "binding user" id)
         (.bindOne Users id $scope "user")
         (.find Users id)))))

(def.controller jiksnu.FollowButtonController
  [$scope app Users]
  (aset $scope "app" app)
  (aset $scope "followLabel" "Follow")
  (aset $scope "isFollowing"
     (fn []
       (and (not (.isActor $scope))
            ;; TODO: write follow checking code
            )))
  (aset $scope "isActor"
     (fn []
       (when-let [user (.-user app)]
         (= (.-_id (.-item $scope))
            (.-_id user)))))
  (aset $scope "submit"
     (fn []
       (js/console.log "Submit button pressed" app)
       (let [item (.-item $scope)]
         (if (.isFollowing $scope)
           (.follow app item)
           (.unfollow app item))))))

(def.controller jiksnu.LeftColumnController
  [$scope $http]
  (aset $scope "groups" (clj->js helpers/nav-info)))

(def.controller jiksnu.ListFollowersController
  [$scope subpageService Users]
  (aset $scope "formShown" false)
  (aset $scope "toggle" (fn [] (! $scope.formShown (not (? $scope.formShown)))))
  (helpers/init-subpage $scope subpageService Users "followers"))

(def.controller jiksnu.ListFollowingController
  [$scope subpageService Users]
  (aset $scope "formShown" false)
  (aset $scope "toggle" (fn [] (! $scope.formShown (not (? $scope.formShown)))))
  (helpers/init-subpage $scope subpageService Users "following"))

(def.controller jiksnu.ListGroupsController
  [$scope subpageService Users]
  (aset $scope "formShown" false)
  (aset $scope "toggle" (fn [] (! $scope.formShown (not (? $scope.formShown)))))
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
  (js/console.log "Loading NavBarController")
  (aset $scope "loaded" false)
  (helpers/setup-hotkeys hotkeys $state)

  (.$watch $scope
           (fn []  (.-data app))
           (fn [d]
             (when (.-loaded $scope)
               (js/console.log "Running navbarcontroller watcher")
               (aset $scope "app" d)
               (-> (.getUser app)
                   (.then (fn [user] (! app.user user)))))))

  (aset $scope "app2" app)
  (aset $scope "logout" (.-logout app))

  (-> (.fetchStatus app)
      (.then (fn []
               (js/console.log "Status Loaded")
               (aset $scope "loaded" true)))))

(def.controller jiksnu.NewPostController
  [$scope $rootScope geolocation app pageService]
  (js/console.log "Loading New Post Controller")
  (let [default-form {:source "web"
                      :privacy "public"
                      :title ""
                      :geo {:latitude nil
                            :longitude nil}
                      :content ""}]
    (.$watch $scope #(? app.data)          (fn [data] (aset $scope "app" data)))
    (.$watch $scope #(? $scope.form.shown) (fn [shown?] (when shown? (.getLocation $scope))))
    (aset $scope "getLocation"  (fn [] (-> (.getLocation geolocation)
                                          (.then (fn [data]
                                                   (let [geo (? $scope.activity.geo)
                                                         coords (.-coords data)]
                                                     (aset geo "latitude" (.-latitude coords))
                                                     (aset geo "longitude" (.-longitude coords))))))))
    (aset $scope "addStream" (fn [id]
                               (let [streams (.-streams (.-activity $scope))]
                                 (if (not-any? (partial = id) streams)
                                   (.push streams id)))))
    (aset $scope "fetchStreams" (fn [] (-> (.getUser app)
                                          (.then (fn [user]
                                                   (js/console.log "Got User" user)
                                                   (-> (.getStreams user)
                                                       (.then (fn [streams]
                                                                (js/console.log "Got Streams" streams)
                                                                (! $scope.streams streams)))))))))
    (aset $scope "toggle"       (fn []
                                  (js/console.debug "Toggling New Post form")
                                  (! $scope.form.shown (not $scope.form.shown))
                                  (when (? $scope.form.shown)
                                    (.fetchStreams $scope))))
    (aset $scope "reset"        (fn []
                                  (! $scope.activity (clj->js default-form))
                                  (aset (.-activity $scope) "streams" (arr))))
    (aset $scope "submit"       (fn [] (-> (.post app $scope.activity)
                                          (.then (fn []
                                                   (.reset $scope)
                                                   (.toggle $scope)
                                                   (.$broadcast $rootScope "updateCollection"))))))
    (.reset $scope)))

(def.controller jiksnu.RegisterPageController
  [app $scope]
  (aset $scope "register" (fn []
                            (-> (.register app $scope)
                                (.then (fn [data]
                                         (aset (.-data app) "user" (.-user (.-data data)))
                                         (-> (.fetchStatus app)
                                             (.then (fn [] (.go app "home"))))))))))

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
  (! $scope.init (fn [id]
                   (.bindOne Domains id $scope "domain")
                   (-> (.find Domains id)
                       (.then (fn [] (! $scope.loaded true))))))
  (.init $scope (.-_id $stateParams)))

(def.controller jiksnu.ShowConversationController
  [$scope $stateParams Conversations app]
  (js/console.log "loading ShowConversationController")

  (aset $scope "loaded" false)
  (aset $scope "init" (fn [id]
                        (.bindOne Conversations id $scope "conversation")
                        (-> (.find Conversations id)
                            (.then (.-fetchActivities app))
                            (.then (fn [] (aset $scope "loaded" true))))))

  (aset $scope "fetchActivities" (fn [conversation]
                                   (js/console.log "fetch activities")
                                   (-> (.getActivities conversation)
                                       (.then  (fn [response]
                                                 (js/console.log "Activities" response)
                                                 (aset $scope "activities" (.-body response)))))))

  (.init $scope (.-_id $stateParams)))

(def.controller jiksnu.ShowGroupController
  [$scope $http $stateParams Groups]
  (js/console.log "loading ShowGroupController")
  (aset $scope "loaded" false)
  (aset $scope "addAdmin" (fn [& opts] (js/console.log opts)))
  (aset $scope "addMember" (fn [& opts] (js/console.log opts)))
  (aset $scope "init"
        (fn [id]
          (.bindOne Groups id $scope "group")
          (-> (.find Groups id)
              (.then (fn [data]
                       (aset $scope "group" data)
                       (aset $scope "loaded" true))))))

  (.init $scope (.-_id $stateParams)))

(def.controller jiksnu.ShowStreamMinimalController
  [$scope Streams]
  (aset $scope "loaded" false)
  (aset $scope "init" (fn [id]
                        (.bindOne Streams id $scope "stream")
                        (-> (.find Streams id)
                            (.then (fn [stream]
                                     (aset $scope "stream" stream)
                                     (aset $scope "loaded" true))))))

  (aset $scope "toggle" (fn []
                          (let [shown? (not (.-formShown $scope))]
                            (aset $scope "formShown" shown?)
                            (aset $scope "btnLabel" (if shown? "-" "+")))))

  (let [id (.-id $scope)]
    (.init $scope id)))

(def.controller jiksnu.ShowUserController
  [$scope $stateParams Users]
  (let [username (.-username $stateParams)
        domain (.-domain $stateParams)
        id (or (.-_id $stateParams)
               (str "acct:" username "@" domain))]
    (! $scope.init
       (fn [id]
         (! $scope.loaded false)
         (js/console.info "binding user" id)
         (.bindOne Users id $scope "user")
         (-> (.find Users id)
             (.then (fn [user] (! $scope.loaded true))))))
    (.init $scope id)))
