(ns jiksnu.controllers
  (:require jiksnu.app
            jiksnu.models
            [jiksnu.helpers :as helpers]
            jiksnu.services
            [jiksnu.templates :as templates]
            [taoensso.timbre :as timbre])
  (:use-macros [gyr.core :only [def.controller]]
               [jiksnu.macros :only [page-controller]]
               [purnam.core :only [? !]]))

(def.controller jiksnu.AdminActivitiesController
  [$scope $http]
  (set! (.-init $scope) (helpers/fetch-page $scope $http "/model/activities.json"))
  (.init $scope))

(def.controller jiksnu.AdminConversationsController
  [$scope $http]
  (set! (.-init $scope) (helpers/fetch-page $scope $http "/admin/conversations.json"))
  (.init $scope))

(def.controller jiksnu.AdminGroupsController [$scope])

(def.controller jiksnu.AdminUsersController
  [$scope $http]
  (set! (.-init $scope) (helpers/fetch-page $scope $http "/admin/users.json"))
  (.init $scope))

(def.controller jiksnu.AppController [])

(def.controller jiksnu.AvatarPageController [])

(def.controller jiksnu.DebugController [$scope $filter app]
  ;; (.$watch $scope
  ;;          (.-isCollapsed $scope)
  ;;          (fn [v]
  ;;            (if v "")
  ;;            ))

  (set! (.-visible $scope) #(.. app -data -debug))

  (set! (.-formattedCode $scope)
        #(($filter "json") (.-expr $scope))))

(def.controller jiksnu.DisplayAvatarController
  [$scope Users]
  (set! (.-init $scope)
        (fn [id]
          (timbre/debug "Displaying avatar for " id)
          (when (and id (not= id ""))
            (set! (.-size $scope) 32)
            ;; (js/console.info "binding user" id)
            (.bindOne Users id $scope "user")
            (.find Users id)))))

(def refresh-followers "refresh-followers")

(def.controller jiksnu.FollowButtonController
  [$scope app $q $rootScope]
  (set! (.-app $scope) app)
  (set! (.-loaded $scope) false)

  (set! (.-init $scope)
        (fn []
          (js/console.log "init")
          (set! (.-loaded $scope) false)
          (when-let [d (.isFollowing $scope)]
            (.then d (fn [following]
                       (set! (.-following $scope) following)
                       (set! (.-followLabel $scope) (if (.-following $scope) "Unfollow" "Follow"))
                       (set! (.-loaded $scope) true))))))

  (set! (.-isFollowing $scope)
        (fn []
          (let [actor-id (.getUserId app)
                user (.-item $scope)
                user-id (.-_id user)
                some-follower (fn [fs] (some #(= (.-from %) actor-id) (.-items fs)))]
            (if (not= actor-id user-id)
              (let [d (.getFollowers user)]
                (.then d some-follower))
                                     (.resolve (.defer $q) nil)))))

  (set! (.-isActor $scope)
        (fn []
          (when-let [user (.-user app)]
            (= (.-_id (.-item $scope))
               (.-_id user)))))

  (set! (.-submit $scope)
        (fn []
          (let [item (.-item $scope)]
            (-> (if (.-following $scope)
                  (.unfollow app item)
                  (.follow app item))
                (.then (fn []
                         (.init $scope)
                         (.$broadcast $rootScope refresh-followers)))))))

  (.$on $scope refresh-followers (.-init $scope))

  (.init $scope))

(def.controller jiksnu.LeftColumnController
  [$scope $http]
  (aset $scope "groups" (clj->js helpers/nav-info)))

(def.controller jiksnu.ListFollowersController
  [$scope subpageService Users]
  (set! (.-formShown $scope) false)
  (set! (.-toggle $scope) (fn [] (set! (.-formShown $scope) (not (.-formShown $scope)))))
  (.$on $scope refresh-followers (fn [] (.init $scope)))
  (helpers/init-subpage $scope subpageService Users "followers"))

(def.controller jiksnu.ListFollowingController
  [$scope subpageService Users]
  (set! (.-formShown $scope) false)
  (set! (.-toggle $scope) (fn [] (set! (.-formShown $scope) (not (.-formShown $scope)))))
  (.$on $scope refresh-followers (fn [] (.init $scope)))
  (helpers/init-subpage $scope subpageService Users "following"))

(def.controller jiksnu.ListGroupsController
  [$scope subpageService Users]
  (aset $scope "formShown" false)
  (aset $scope "toggle" (fn [] (! $scope.formShown (not (? $scope.formShown)))))
  (helpers/init-subpage $scope subpageService Users "groups"))

(def.controller jiksnu.ListStreamsController
  [$scope app subpageService Users]
  (set! (.-formShown $scope) false)
  (set! (.-toggle $scope)
        (fn [] (set! (.-formShown $scope) (not (.-formShown $scope)))
          (.updateLabel $scope)))
  (set! (.-addStream $scope)
        (fn []
          (if-let [stream-name (? $scope.stream.name)]
            (.addStream app stream-name)
            (throw (js/Error. "Could not determine stream name")))))
  (set! (.-updateLabel $scope) (fn []
                                 (set! (.-btnLabel $scope)
                                       (if (.-formShown $scope) "-" "+"))))
  (helpers/init-subpage $scope subpageService Users "streams")
  (.updateLabel $scope))

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
  (set! (.-app2 $scope) app)
  (set! (.-loaded $scope) false)
  (set! (.-logout $scope) (.-logout app))
  (set! (.-navbarCollapsed $scope) true)

  (helpers/setup-hotkeys hotkeys $state)
  (let [on-data-changed
        (fn [d]
          (when (.-loaded $scope)
            (timbre/debug "Running navbarcontroller watcher")
            (set! (.-app $scope) d)
            (let [p (.getUser app)]
              (-> p (.then (fn [user] (set! (.-user app) user)))))))]
    (.$watch $scope #(.-data app) on-data-changed))

  (-> (.fetchStatus app)
      (.then (fn [] (set! (.-loaded $scope) true)))))

(def.controller jiksnu.NewGroupController
  [$scope]

  )

(def.controller jiksnu.NewPostController
  [$scope $rootScope geolocation app pageService subpageService $filter Users]
  (timbre/debug "Loading New Post Controller")
  (let [default-form #js {:source "web"
                          :privacy "public"
                          :title ""
                          :geo #js {:latitude nil
                                :longitude nil}
                          :content ""}
        get-location (fn []
                       (-> (.getLocation geolocation)
                           (.then (fn [data]
                                    (let [geo (.. $scope -activity -geo)
                                          coords (.-coords data)]
                                      (set! (.-latitude geo) (.-latitude coords))
                                      (set! (.-longitude geo) (.-longitude coords)))))))]
    (.$watch $scope #(? $scope.form.shown) #(when % (get-location)))

    (set! (.-form $scope) #js {:shown false})

    (set! (.-enabled $scope)
          (fn []
            (.-data app)))

    (set! (.-addStream $scope)
          (fn [id]
            (timbre/debug "adding stream" id)
            (let [streams (.. $scope -activity -streams)]
              (if (not-any? (partial = id) streams)
                (.push streams id)))))

    (set! (.-fetchStreams $scope)
          (fn []
            (timbre/debug "fetching streams")
            (-> (.getUser app)
                (.then (fn [user]
                         (timbre/debug "Got User" user)
                         (-> (.getStreams user)
                             (.then (fn [streams]
                                      (timbre/debug "Got Streams" streams)
                                      (set! (.-streams $scope) streams)))))))))

    (set! (.-toggle $scope)
          (fn []
            (timbre/debug "Toggling New Post form")
            (set! (.. $scope -form -shown)
                  (not (.. $scope -form -shown)))
            (when (.. $scope -form -shown)
              (.fetchStreams $scope))))

    (set! (.-reset $scope)
          (fn []
            (set! (.-activity $scope) default-form)
            (set! (.. $scope -activity -streams) #js [])))

    (set! (.-submit $scope)
          (fn []
            (-> (.post app (.-activity $scope))
                (.then (fn []
                         (.reset $scope)
                         (.toggle $scope)
                         (.$broadcast $rootScope "updateCollection"))))))

    (helpers/init-subpage $scope subpageService Users "streams")
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
  (.$watch $scope #(.-data app) #(set! (.-app $scope) %))
  (set! (.-foo $scope) "bar"))

(def.controller jiksnu.SettingsPageController [])

(def.controller jiksnu.SubscribersWidgetController
  [$scope])

(def.controller jiksnu.ShowActivityController
  [$scope $stateParams Activities]
  (set! (.-loaded $scope) false)

  (set! (.-init $scope)
     (fn [id]
       (when (and id (not= id ""))
         (.bindOne Activities id $scope "activity")
         (-> (.find Activities id)
             (.then (fn [] (! $scope.loaded true)))))))

  (.init $scope (.-id $scope)))

(def.controller jiksnu.ShowDomainController
  [$scope $stateParams Domains]
  (set! (.-loaded $scope) false)
  (set! (.-init $scope)
        (fn [id]
          (.bindOne Domains id $scope "domain")
          (-> (.find Domains id)
              (.then (fn [] (set! (.-loaded $scope) true))))))
  (.init $scope (.-_id $stateParams)))

(def.controller jiksnu.ShowConversationController
  [$scope $stateParams Conversations app]
  (timbre/debug "loading ShowConversationController")
  (set! (.-loaded $scope) false)

  (set! (.-init $scope)
        (fn [id]
          (.bindOne Conversations id $scope "conversation")
          (-> (.find Conversations id)
              (.then (.-fetchActivities app))
              (.then (fn [] (set! (.-loaded $scope) true))))))

  (set! (.-fetchActivities $scope)
        (fn [conversation]
          (timbre/debug "fetch activities")
          (-> (.getActivities conversation)
              (.then  (fn [response]
                        (timbre/debug "Activities" response)
                        (aset $scope "activities" (.-body response)))))))

  (.init $scope (.-_id $stateParams)))

(def.controller jiksnu.ShowGroupController
  [$scope $http $stateParams Groups]
  (timbre/debug "loading ShowGroupController")
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

(def.controller jiksnu.ShowStreamController
  [$scope $http $stateParams Streams]
  (js/console.log "loading ShowStreamController")
  (let [model Streams
        label "stream"]
    (set! (.-loaded $scope) false)
    (set! (.-init $scope)
          (fn [id]
            (.bindOne model id $scope label)
            (-> (.find model id)
                (.then (fn [data]
                         (aset $scope label data)
                         (set! (.-loaded $scope) true))))))

    (.init $scope (.-_id $stateParams))))

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
         ;; (js/console.info "binding user" id)
         (.bindOne Users id $scope "user")
         (-> (.find Users id)
             (.then (fn [user] (! $scope.loaded true))))))
    (.init $scope id)))
