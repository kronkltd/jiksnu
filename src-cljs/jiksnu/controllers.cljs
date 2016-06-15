(ns jiksnu.controllers
  (:require jiksnu.app
            jiksnu.models
            [jiksnu.helpers :as helpers]
            jiksnu.services
            [taoensso.timbre :as timbre])
  (:use-macros [gyr.core :only [def.controller]]
               [jiksnu.macros :only [page-controller]]
               [purnam.core :only [? !]]))

(def.controller jiksnu.AppController [])

(def.controller jiksnu.AsModelController
  [$scope DS]
  (let [collection-name (.-model $scope)]
    (set! (.-init $scope)
          (fn [id]
            (timbre/debugf "init as model %s(%s)" collection-name id)
            (.bindOne DS collection-name id $scope "item")
            (.find DS collection-name id)))
    (.init $scope (.-id $scope))))

(def.controller jiksnu.AvatarPageController [])

(def.controller jiksnu.AuthorizeClientController
  [$location $scope $stateParams app RequestTokens]
  (timbre/info "Location: " $location)
  (timbre/info "State Params: " $stateParams)
  (set! (.-id $scope) (aget (.search $location) "oauth_token"))
  (helpers/init-item $scope $stateParams app RequestTokens))

(def.controller jiksnu.DebugController [$scope $filter app]
  (set! (.-visible $scope) #(.. app -data -debug))

  (set! (.-formattedCode $scope)
        #(($filter "json") (.-expr $scope))))

(def.controller jiksnu.DisplayAvatarController
  [$scope Users]
  (set! (.-init $scope)
        (fn []
          (when-let [id (.-id $scope)]
            (timbre/debugf "Displaying avatar for %s" id)
            (set! (.-size $scope) (or (.-size $scope) 32))
            (.bindOne Users id $scope "user")
            (.find Users id))))
  (.init $scope))

(def refresh-followers "refresh-followers")

(def.controller jiksnu.FollowButtonController
  [$scope app $q $rootScope Subscriptions]
  (set! (.-app $scope) app)
  (set! (.-loaded $scope) false)

  (set! (.-isActor $scope)
        (fn []
          (if-let [item-id (.-_id (.-item $scope))]
            (if-let [user-id (.getUserId app)]
              (do
                (set! (.-authenticated $scope) true)
                (= item-id user-id))
              (do
                (set! (.-authenticated $scope) false)
                false))
            (throw "No item bound to scope"))))

  (set! (.-init $scope)
        (fn []
          (let [actor (.isActor $scope)]
            (set! (.-actor $scope) actor)
            (when-not actor
              (-> (.isFollowing $scope)
                  (.then (fn [following]
                           (set! (.-following $scope) following)
                           (set! (.-followLabel $scope)
                                 (if (.-following $scope) "Unfollow" "Follow"))
                           (set! (.-loaded $scope) true))))))))

  (set! (.-isFollowing $scope)
        (fn []
          ($q
           (fn [resolve reject]
             (if-let [user (.-item $scope)]
               (let [user-id (.-_id user)]
                 (timbre/debug "Checking if following")
                 (.. app
                     (getUser)
                     (then #(some-> % .getFollowing))
                     (then (fn [page]
                             (timbre/debug "Got page")
                             (when page
                               (->> (.-items page)
                                    (map (.-find Subscriptions))
                                    clj->js
                                    (.all $q)))))
                     (then (fn [subscriptions]
                             (timbre/debug "got subscriptions")
                             (some #(#{user-id} (.-to %)) subscriptions)))
                     (then resolve)))
               (do
                 (timbre/warn "No item bound to scope")
                 (reject)))))))

  (set! (.-submit $scope)
        (fn []
          (let [item (.-item $scope)]
            (-> (if (.-following $scope)
                  (.unfollow app item)
                  (.follow app item))
                (.then (fn []
                         (.init $scope)
                         (.$broadcast $rootScope refresh-followers)))))))

  (.$watch $scope
           (fn [] (.-data app))
           (fn [data old-data] (.init $scope)))
  (.$on $scope refresh-followers (.-init $scope)))

(def.controller jiksnu.LeftColumnController
  [$scope $http]
  (set! (.-groups $scope) (clj->js helpers/nav-info)))

(defn get-toggle-fn
  [$scope]
  (fn [] (set! (.-formShown $scope) (not (.-formShown $scope)))))

(def.controller jiksnu.ListActivitiesController
  [$scope subpageService Users]
  (set! (.-formShown $scope) false)
  (set! (.-toggle $scope) (get-toggle-fn $scope))
  (set! (.-refresh $scope) (fn [] (.$broadcast $scope refresh-followers))))

(def.controller jiksnu.ListFollowersController
  [$scope subpageService Users]
  (set! (.-formShown $scope) false)
  (set! (.-toggle $scope) (get-toggle-fn $scope))
  (set! (.-refresh $scope) (fn [] (.$broadcast $scope refresh-followers))))

(def.controller jiksnu.ListFollowingController
  [$scope subpageService Users]
  (set! (.-formShown $scope) false)
  (set! (.-toggle $scope) (get-toggle-fn $scope))
  (set! (.-refresh $scope) (fn [] (.$broadcast $scope refresh-followers))))

(def.controller jiksnu.ListGroupsController
  [$scope subpageService Users]
  (set! (.-formShown $scope) false)
  (set! (.-toggle $scope) (get-toggle-fn $scope))
  (helpers/init-subpage $scope subpageService Users "groups"))

(def.controller jiksnu.ListGroupAdminsController
  [$scope subpageService Groups]
  (set! (.-formShown $scope) false)
  (set! (.-toggle $scope) (fn [] (set! (.-formShown $scope) (not (.-formShown $scope)))))
  (helpers/init-subpage $scope subpageService Groups "admins"))

(def.controller jiksnu.ListGroupMembersController
  [$scope subpageService Groups]
  (set! (.-formShown $scope) false)
  (set! (.-toggle $scope) (get-toggle-fn $scope))
  (helpers/init-subpage $scope subpageService Groups "members"))

(def.controller jiksnu.ListNotificationsController
  [$scope subpageService Notifications]
  (set! (.-formShown $scope) false)
  (set! (.-toggle $scope) (fn [] (set! (.-formShown $scope) (not (.-formShown $scope)))))
  (helpers/init-subpage $scope subpageService Notifications "notifications"))

(def.controller jiksnu.ListStreamsController
  [$scope app subpageService Users]
  (set! (.-formShown $scope) false)
  (set! (.-app $scope) app)
  (set! (.-toggle $scope) (get-toggle-fn $scope))

  (.$watch $scope
           (.-formShown $scope)
           (fn [state]
             (.updateLabel $scope)))

  (set! (.-deleteStream $scope)
        (fn [item]
          (.deleteStream app item)))

  (set! (.-addStream $scope)
        (fn []
          (if-let [stream-name (.. $scope -stream -name)]
            (.addStream app stream-name)
            (throw (js/Error. "Could not determine stream name")))))

  (set! (.-updateLabel $scope)
        (fn []
          (set! (.-btnLabel $scope)
                (if (.-formShown $scope) "-" "+"))))

  (helpers/init-subpage $scope subpageService Users "streams")
  (.updateLabel $scope))

(def.controller jiksnu.LoginPageController
  [$scope $state app Notification]
  (set! (.-login $scope)
        (fn []
          (let [username (.-username $scope)
                password (.-password $scope)]
            (-> (.login app username password)
                (.then (fn [r] (.go $state "home"))
                       (fn [e] (.warning Notification "login failed"))))))))

(def.controller jiksnu.LogoutController [])

(page-controller Activities       "activities")
(page-controller Clients          "clients")
(page-controller Conversations    "conversations")
(page-controller Domains          "domains")
(page-controller FeedSources      "feed-sources")
(page-controller Groups           "groups")
(page-controller GroupMemberships "group-memberships")
(page-controller Likes            "likes")
(page-controller Notifications    "notifications")
(page-controller RequestTokens    "request-tokens")
(page-controller Resources        "resources")
(page-controller Streams          "streams")
(page-controller Subscriptions    "subscriptions")
(page-controller Users            "users")

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
  [$scope app $http]
  (let [default-form #js {}]

    (set! (.-init $scope)
          (fn []
            (timbre/debug "init NewGroupController")
            (.reset $scope)))

    (set! (.-reset $scope)
          (fn []
            (timbre/info "reset")
            (set! (.-form $scope) default-form)))

    (set! (.-submit $scope)
          (fn []
            (timbre/info "Submitting group form")
            (let [params (.-group $scope)
                  path "/model/groups"]
              (-> (.post $http path params)
                  (.then (fn [r]
                           (timbre/info "Submitted")
                           (js/console.info r))
                         (fn [r]
                           (timbre/info "Failed")
                           (js/console.info r)))))))
    (.init $scope)))

(def.controller jiksnu.NewPostController
  [$scope $rootScope geolocation app pageService subpageService $filter Streams Users]
  (timbre/debug "Loading New Post Controller")
  (set! (.-addStream $scope)
        (fn [id]
          (timbre/debug "adding stream" id)
          (let [streams (.. $scope -activity -streams)]
            (if (not-any? (partial = id) streams)
              (.push streams id)))))
  (set! (.-app $scope) app)
  (set! (.-defaultForm $scope) #js {:source "web"
                                    :privacy "public"
                                    :title ""
                                    :geo #js {:latitude nil
                                              :longitude nil}
                                    :content ""})
  (set! (.-enabled $scope) (fn [] (.-data app)))
  (set! (.-fetchStreams $scope)
        (fn []
          (timbre/debug "fetching streams")
          (.. app
              (getUser)
              (then (fn [user]
                      (timbre/debugf "Got User - %s" user)
                      (.getStreams user)))
              (then (fn [streams]
                      (timbre/debugf "Got Streams - %s" streams)
                      (set! (.-streams $scope) streams))))))
  (set! (.-form $scope) #js {:shown false})
  (set! (.-getLocataion $scope)
        (fn []
          (.. geolocation
              (getLocation)
              (then (fn [data]
                      (let [geo (.. $scope -activity -geo)
                            coords (.-coords data)]
                        (set! (.-latitude geo) (.-latitude coords))
                        (set! (.-longitude geo) (.-longitude coords))))))))
  (set! (.-reset $scope)
        (fn []
          (set! (.-activity $scope) (.-defaultForm $scope))
          (set! (.. $scope -activity -streams) #js [])))
  (set! (.-submit $scope)
        (fn []
          (.. app
              (post (.-activity $scope))
              (then (fn []
                      (.reset $scope)
                      (.toggle $scope)
                      (.refresh app))))))
  (set! (.-toggle $scope)
        (fn []
          (timbre/debug "Toggling New Post form")
          (set! (.. $scope -form -shown)
                (not (.. $scope -form -shown)))
          (when (.. $scope -form -shown)
            (.getLocation $scope)
            (.fetchStreams $scope))))
  (helpers/init-subpage $scope subpageService Users "streams")
  (.reset $scope))

(def.controller jiksnu.NewStreamController
  [$scope $rootScope app]
  (set! (.-app $scope) app)
  (set! (.-stream $scope) #js {})
  (set! (.-submit $scope)
        (fn [args]
          (let [stream-name (.-name (.-stream $scope))]
            (set! (.-name $scope) "")
            (.. app
                (addStream stream-name)
                (then (fn [stream]
                        (timbre/info "Added Stream" stream)
                        (.refresh app))))))))

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
  [$scope $stateParams Activities app $rootScope]
  (set! (.-app $scope) app)

  (set! (.-likeActivity $scope)
        (fn [activity]
          (.invokeAction app "activity" "like" (.-id $scope))))

  (helpers/init-item $scope $stateParams app Activities))

(def.controller jiksnu.ShowDomainController
  [$scope $stateParams app Domains]
  (set! (.-loaded $scope) false)
  (helpers/init-item $scope $stateParams app Domains))

(def.controller jiksnu.ShowClientController
  [$scope $stateParams app Clients]
  (helpers/init-item $scope $stateParams app Clients))

(def.controller jiksnu.ShowClientMinimalController
  [$scope $stateParams app Clients]
  (helpers/init-item $scope $stateParams app Clients))

(def.controller jiksnu.ShowConversationController
  [$scope $stateParams Conversations app $rootScope]
  (helpers/init-item $scope $stateParams app Conversations)
  (set! (.-app $scope) app))

(def.controller jiksnu.ShowFollowersMinimalController
  [$scope $stateParams app Subscriptions]
  (helpers/init-item $scope $stateParams app Subscriptions))

(def.controller jiksnu.ShowFollowingMinimalController
  [$scope $stateParams app Subscriptions]
  (helpers/init-item $scope $stateParams app Subscriptions))

(def.controller jiksnu.ShowGroupController
  [$scope $http $stateParams app Groups]
  (timbre/debug "loading ShowGroupController")
  (set! (.-join $scope)
        (fn []
          (timbre/info "Joining group")
          (let [id (.-_id (.-item $scope))]
            (.invokeAction app "group" "join" id))))
  (helpers/init-item $scope $stateParams app Groups))

(def.controller jiksnu.ShowGroupMinimalController
  [$scope $stateParams app Groups]
  (helpers/init-item $scope $stateParams app Groups))

(def.controller jiksnu.ShowGroupMembershipMinimalController
  [$scope $stateParams app GroupMemberships]
  (helpers/init-item $scope $stateParams app GroupMemberships))

(def.controller jiksnu.ShowLikeController
  [$scope $stateParams app Likes]
  (helpers/init-item $scope $stateParams app Likes))

(def.controller jiksnu.ShowLikedByController
  [$scope $stateParams app Likes]
  (helpers/init-item $scope $stateParams app Likes))

(def.controller jiksnu.ShowNotificationController
  [$scope $stateParams app Notifications]
  (helpers/init-item $scope $stateParams app Notifications))

(def.controller jiksnu.ShowRequestTokenController
  [$scope $http $stateParams app RequestTokens]
  (helpers/init-item $scope $stateParams app RequestTokens))

(def.controller jiksnu.ShowStreamController
  [$scope $http $stateParams app Streams]
  (helpers/init-item $scope $stateParams app Streams))

(def.controller jiksnu.ShowStreamMinimalController
  [$scope $stateParams app Streams]
  (helpers/init-item $scope $stateParams app Streams)
  (set! (.-toggle $scope)
        (fn []
          (let [shown? (not (.-formShown $scope))]
            (set! (.-formShown $scope) shown?)
            (set! (.-btnLabel $scope) (if shown? "-" "+"))))))

(def.controller jiksnu.ShowSubscriptionController
  [$scope $stateParams app Subscriptions]
  (helpers/init-item $scope $stateParams app Subscriptions))

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
  [$scope subpageService $rootScope]
  (set! (.-loaded $scope) false)
  (if-let [subpage (.-subpage $scope)]
    (do
      (timbre/debug "initialize subpage controller" subpage)
      (set! (.-refresh $scope) (fn [] (.init $scope (.-item $scope))))

      ;; (.$on $scope refresh-followers
      ;;       (fn []
      ;;         (timbre/debug "received refresh event")
      ;;         (.refresh $scope)))

      ;; (.$on $rootScope refresh-followers
      ;;       (fn []
      ;;         (timbre/debug "received refresh event on root")
      ;;         (.refresh $scope)))

      (.$on $scope "refresh-page"
            (fn []
              (timbre/debug "received refresh event on subpage scope")
              (.refresh $scope)))

      (set! (.-init $scope)
            (fn [item]
              (if item
                (do
                  (set! (.-item $scope) item)
                  (set! (.-loaded $scope) false)
                  (timbre/debugf "Refreshing subpage: %s(%s)=>%s"
                                 (.. item -constructor -name)
                                 (.-_id item)
                                 subpage)
                  (-> (.fetch subpageService item subpage)
                      (.then (fn [page]
                               (set! (.-loaded $scope) true)
                               (set! (.-page $scope) page)))))
                (throw (str "parent item not bound for subpage: " subpage)))))
      (.refresh $scope))
    (throw "Subpage not specified")))

(def.controller jiksnu.SubscribersWidgetController
  [$scope])
