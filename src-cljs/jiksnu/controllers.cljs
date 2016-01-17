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
  (set! (.-visible $scope) #(.. app -data -debug))

  (set! (.-formattedCode $scope)
        #(($filter "json") (.-expr $scope))))

(def.controller jiksnu.DisplayAvatarController
  [$scope Users]
  (set! (.-init $scope)
        (fn [id]
          #_(timbre/debug "Displaying avatar for " id)
          (when (and id (not= id ""))
            (set! (.-size $scope) 32)
            (.bindOne Users id $scope "user")
            (.find Users id)))))

(def refresh-followers "refresh-followers")

(def.controller jiksnu.FollowButtonController
  [$scope app $q $rootScope]
  (set! (.-app $scope) app)
  (set! (.-loaded $scope) false)

  (set! (.-init $scope)
        (fn []
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
  (set! (.-groups $scope) (clj->js helpers/nav-info)))

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
  (set! (.-formShown $scope) false)
  (set! (.-toggle $scope) (fn [] (set! (.-formShown $scope) (not (.-formShown $scope)))))
  (helpers/init-subpage $scope subpageService Users "groups"))

(def.controller jiksnu.ListStreamsController
  [$scope app subpageService Users]
  (set! (.-formShown $scope) false)
  (set! (.-toggle $scope)
        (fn [] (set! (.-formShown $scope) (not (.-formShown $scope)))
          (.updateLabel $scope)))

  (set! (.-deleteStream $scope) #(.deleteStream app %))

  (set! (.-addStream $scope)
        (fn []
          (if-let [stream-name (.. $scope -stream -name)]
            (.addStream app stream-name)
            (throw (js/Error. "Could not determine stream name")))))
  (set! (.-updateLabel $scope) (fn []
                                 (set! (.-btnLabel $scope)
                                       (if (.-formShown $scope) "-" "+"))))
  (helpers/init-subpage $scope subpageService Users "streams")
  (.updateLabel $scope))

(def.controller jiksnu.LoginPageController
  [$scope app]
  (set! (.-login $scope)
        (fn []
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
(page-controller Subscriptions "subscriptions" [])
(page-controller Users         "users"         [])

(def.controller jiksnu.NavBarController
  [$scope app hotkeys $state]
  (set! (.-app2 $scope) app)
  (set! (.-loaded $scope) false)
  (set! (.-logout $scope) (.-logout app))
  (set! (.-navbarCollapsed $scope) true)

  (helpers/setup-hotkeys hotkeys $state)
  (.$watch $scope
           #(.-data app)
           (fn [d]
             (when (.-loaded $scope)
               (timbre/debug "Running navbarcontroller watcher")
               (set! (.-app $scope) d)
               (-> (.getUser app)
                   (.then (fn [user]
                            (timbre/debug "setting app user")
                            (set! (.-user app) user)))))))

  (-> (.fetchStatus app)
      (.then (fn [] (set! (.-loaded $scope) true)))))

(def.controller jiksnu.NewGroupController
  [$scope]

  )

(def.controller jiksnu.NewPostController
  [$scope $rootScope geolocation app pageService subpageService $filter Users]
  #_(timbre/debug "Loading New Post Controller")
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
              (get-location)
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

(def.controller jiksnu.NewStreamController
  [$scope $rootScope app]
  (set! (.-app $scope) app)
  (set! (.-stream $scope) #js {})
  (set! (.-submit $scope)
        (fn [args]
          (let [stream-name (.-name (.-stream $scope))]
            (set! (.-name $scope) "")
            (-> (.addStream app stream-name)
                (.then (fn [stream]
                         (timbre/info "Added Stream" stream)
                         (.$broadcast $rootScope "updateCollection"))))))))

(def.controller jiksnu.RegisterPageController
  [app $scope]
  (set! (.-register $scope)
        (fn []
          (-> (.register app $scope)
              (.then (fn [data]
                       ;; Should we copy the whole data object?
                       (set! (.. app -data -user) (.. data -data -user))
                       (-> (.fetchStatus app)
                           (.then #(.go app "home")))))))))

(def.controller jiksnu.RightColumnController
  [$scope app]
  (.$watch $scope #(.-data app) #(set! (.-app $scope) %))
  (set! (.-foo $scope) "bar"))

(def.controller jiksnu.SettingsPageController [])

(def.controller jiksnu.ShowActivityController
  [$scope $stateParams Activities app]
  (set! (.-loaded $scope) false)

  (set! (.-init $scope)
        (fn [id]
          (when (and id (not= id ""))
            (.bindOne Activities id $scope "activity")
            (-> (.find Activities id)
                (.then (fn [] (set! (.-loaded $scope) true)))))))

  (set! (.-deleteRecord $scope)
        (fn [activity]
          (let [msg (str "invoke-action \"activity\", \"delete\", \""
                         (.-id $scope)
                         "\"")]
            (.send app msg))))

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
  (set! (.-loaded $scope) false)

  (set! (.-init $scope)
        (fn [id]
          #_(timbre/debugf "loading ShowConversationController - %s" id)
          (.bindOne Conversations id $scope "conversation")
          (-> (.find Conversations id)
              (.then (fn [conversation]
                       (when conversation
                         (set! (.-item $scope) conversation)
                         (set! (.-loaded $scope) true)))))))

  (set! (.-deleteRecord $scope)
        (fn [item]
          (let [msg (str "invoke-action \"conversation\", \"delete\", \""
                         (.-id $scope)
                         "\"")]
            (.send app msg))))

  (set! (.-fetchActivities $scope)
        (fn [conversation]
          (timbre/debug "fetch activities")
          (-> (.getActivities conversation)
              (.then  (fn [response]
                        (timbre/debug "Activities" response)
                        (set! (.-activities $scope) (.-body response)))))))

  (let [id (or (.-id $scope) (.-_id $stateParams))]
    (.init $scope id)))

(def.controller jiksnu.ShowFollowersMinimalController
  [$scope $stateParams Subscriptions]
  (set! (.-init $scope)
        (fn [id]
          (set! (.-loaded $scope) false)
          (.bindOne Subscriptions id $scope "item")
          (-> (.find Subscriptions id)
              (.then (fn [_] (set! (.-loaded $scope) true))))))

  (let [id (or (.-id $scope) (.-_id $stateParams))]
    (.init $scope id)))

(def.controller jiksnu.ShowFollowingMinimalController
  [$scope $stateParams Subscriptions]
  (set! (.-init $scope)
        (fn [id]
          (set! (.-loaded $scope) false)
          (.bindOne Subscriptions id $scope "item")
          (-> (.find Subscriptions id)
              (.then (fn [_] (set! (.-loaded $scope) true))))))

  (let [id (or (.-id $scope) (.-_id $stateParams))]
    (.init $scope id)))

(def.controller jiksnu.ShowGroupController
  [$scope $http $stateParams Groups]
  (timbre/debug "loading ShowGroupController")
  (set! (.-loaded $scope) false)
  (set! (.-addAdmin $scope)  (fn [& opts] (js/console.log opts)))
  (set! (.-addMember $scope) (fn [& opts] (js/console.log opts)))
  (set! (.-init $scope)
        (fn [id]
          (.bindOne Groups id $scope "group")
          (-> (.find Groups id)
              (.then (fn [data]
                       (set! (.-group $scope) data)
                       (set! (.-loaded $scope) true))))))
  (let [id (.-_id $stateParams)]
    (.init $scope id)))

(def.controller jiksnu.ShowStreamController
  [$scope $http $stateParams Streams]
  (timbre/info "loading ShowStreamController")
  (let [model Streams
        label "stream"]
    (set! (.-loaded $scope) false)
    (set! (.-init $scope)
          (fn [id]
            (.bindOne model id $scope label)
            (-> (.find model id)
                (.then (fn [data]
                         (set! (.-stream $scope) data)
                         (set! (.-loaded $scope) true))))))

    (.init $scope (.-_id $stateParams))))

(def.controller jiksnu.ShowStreamMinimalController
  [$scope Streams]
  (set! (.-loaded $scope) false)
  (set! (.-init $scope)
        (fn [id]
          (.bindOne Streams id $scope "stream")
          (-> (.find Streams id)
              (.then (fn [stream]
                       (set! (.-stream $scope) stream)
                       (set! (.-loaded $scope) true))))))

  (set! (.-toggle $scope)
        (fn []
          (let [shown? (not (.-formShown $scope))]
            (set! (.-formShown $scope) shown?)
            (set! (.-btnLabel $scope) (if shown? "-" "+")))))

  (let [id (.-id $scope)]
    (.init $scope id)))

(def.controller jiksnu.ShowSubscriptionController
  [$scope $stateParams Subscriptions]
  (set! (.-init $scope)
        (fn [id]
          (set! (.-loaded $scope) false)
          (.bindOne Subscriptions id $scope "item")
          (-> (.find Subscriptions id)
              (.then (fn [_] (set! (.-loaded $scope) true))))))

  (let [id (or (.-_id $stateParams) (.-id $scope))]
    (.init $scope id)))

(def.controller jiksnu.ShowUserController
  [$scope $stateParams Users]
  (set! (.-init $scope)
        (fn [id]
          (set! (.-loaded $scope) false)
          (.bindOne Users id $scope "user")
          (-> (.find Users id)
              (.then (fn [_] (set! (.-loaded $scope) true))))))

  (let [id (or (.-id $scope)
               (.-_id $stateParams)
               (when-let [username (.-username $stateParams)]
                 (when-let [domain (.-domain $stateParams)]
                   (str "acct:" username "@" domain))))]
    (.init $scope id)))

(def.controller jiksnu.ShowUserMinimalController
  [$scope $stateParams Users]
  (set! (.-init $scope)
        (fn [id]
          (timbre/infof "init minimal user - %s" id)
          (when id
            (set! (.-loaded $scope) false)
            (.bindOne Users id $scope "item")
            (-> (.find Users id)
                (.then (fn [_] (set! (.-loaded $scope) true)))))))

  (let [id (or (.-id $scope)
               (.-_id $stateParams)
               (when-let [username (.-username $stateParams)]
                 (when-let [domain (.-domain $stateParams)]
                   (str "acct:" username "@" domain))))]
    (.init $scope id)))

(def.controller jiksnu.SubpageController
  [$scope subpageService]
  (set! (.-loaded $scope) false)
  (if-let [item (.-item $scope)]
    (let [subpage (.-subpage $scope)]
      (timbre/debug "subpage controller" (.-_id item) subpage)
      (set! (.-refresh $scope)
            (fn []
              (set! (.-loaded $scope) false)
              (timbre/info "Refreshing subpage" (.-_id item) subpage)
              (-> (.fetch subpageService item subpage)
                  (.then (fn [page]
                           (set! (.-loaded $scope) true)
                           (set! (.-page $scope) page))))))
      (.refresh $scope))
    (timbre/warn "No item bound")))

(def.controller jiksnu.SubscribersWidgetController
  [$scope])
