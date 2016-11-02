(ns jiksnu.controllers
  (:require jiksnu.app
            jiksnu.factories
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
            ;; (timbre/debugf "Displaying avatar for %s" id)
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

(defn get-toggle-fn
  [$scope]
  (fn [] (set! (.-formShown $scope) (not (.-formShown $scope)))))

(def.controller jiksnu.ListActivitiesController
  [$scope app Users]
  (helpers/init-subpage $scope app Users "activities"))

(def.controller jiksnu.ListAlbumsController
  [$scope app Users]
  (helpers/init-subpage $scope app Users "albums"))

(def.controller jiksnu.ListFollowersController
  [$scope app Users]
  (helpers/init-subpage $scope app Users "followers"))

(def.controller jiksnu.ListFollowingController
  [$scope app Users]
  (helpers/init-subpage $scope app Users "following"))

(def.controller jiksnu.ListGroupsController
  [$scope app Users]
  (helpers/init-subpage $scope app Users "groups"))

(def.controller jiksnu.ListGroupAdminsController
  [$scope app Groups]
  (helpers/init-subpage $scope app Groups "admins"))

(def.controller jiksnu.ListGroupMembersController
  [$scope app Groups]
  (helpers/init-subpage $scope app Groups "members"))

(def.controller jiksnu.ListNotificationsController
  [$scope app Users]
  (helpers/init-subpage $scope app Users "notifications"))

(def.controller jiksnu.ListPicturesController
  [$scope app Albums]
  (helpers/init-subpage $scope app Albums "pictures"))

(def.controller jiksnu.ListStreamsController
  [$scope app app Users]

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

  (helpers/init-subpage $scope app Users "streams")
  (.updateLabel $scope))

(def.controller jiksnu.LoginPageController
  [$scope $state app $mdToast]
  (set! (.-login $scope)
        (fn []
          (let [username (.-username $scope)
                password (.-password $scope)]
            (-> (.login app username password)
                (.then (fn [r] (.go $state "home"))
                       (fn [e] (.showSimple $mdToast "login failed"))))))))

(def.controller jiksnu.LogoutController [])

(page-controller Activities       "activities")
(page-controller Albums           "albums")
(page-controller Clients          "clients")
(page-controller Conversations    "conversations")
(page-controller Domains          "domains")
(page-controller FeedSources      "feed-sources")
(page-controller Groups           "groups")
(page-controller GroupMemberships "group-memberships")
(page-controller Likes            "likes")
(page-controller Notifications    "notifications")
(page-controller Pictures         "pictures")
(page-controller RequestTokens    "request-tokens")
(page-controller Resources        "resources")
(page-controller Services         "services")
(page-controller Streams          "streams")
(page-controller Subscriptions    "subscriptions")
(page-controller Users            "users")

(def.controller jiksnu.MainLayoutController
  [$location $mdSidenav $scope app]
  (let [protocol (.protocol $location)
        hostname (.host $location)
        port (.port $location)
        secure? (= protocol "https")
        default-port? (or (and secure?       (= port 443))
                          (and (not secure?) (= port 80)))]
    (set! (.-apiUrl $scope)
          (str "/vendor/swagger-ui/dist/index.html?url="
               protocol "://" hostname
               (when-not default-port? (str ":" port))
               "/api-docs.json"))
    (set! (.-$mdSidenav $scope) $mdSidenav)
    (set! (.-logout $scope) (.-logout app))))

(def.controller jiksnu.NavBarController
  [$mdSidenav $rootScope $scope $state app hotkeys]
  (set! (.-app2 $scope) app)
  (set! (.-loaded $scope) false)
  (set! (.-logout $scope) (.-logout app))
  (set! (.-navbarCollapsed $scope) true)

  (helpers/setup-hotkeys hotkeys $state)

  (.$on $rootScope "$stateChangeSuccess"
        (fn [] (.close ($mdSidenav "left"))))

  (set! (.-init $scope)
        (fn [d]
          (when (.-loaded $scope)
            (timbre/debug "Running navbarcontroller watcher")
            (set! (.-app $scope) d)
            (-> (.getUser app)
                (.then (fn [user]
                         (timbre/debug "setting app user")
                         (set! (.-user app) user)))))))

  (set! (.-toggleSidenav $scope) (fn [] (.toggle ($mdSidenav "left"))))

  (.$watch $scope #(.-data app) (.-init $scope))

  (-> (.fetchStatus app)
      (.then (fn [] (set! (.-loaded $scope) true)))))

(def.controller jiksnu.NewAlbumController
  [$scope app $http]
  (let [default-form #js {}]
    (set! (.-init $scope) #(.reset $scope))
    (set! (.-reset $scope) #(set! (.-album $scope) default-form))

    (set! (.-submit $scope)
          (fn []
            (timbre/info "Submitting album form")
            (let [params (.-album $scope)
                  path "/model/albums"]
              (.post $http path params))))
    (.init $scope)))

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
              (.post $http path params))))
    (.init $scope)))

(def.controller jiksnu.NewPictureController
  [$scope app $http]
  (let [default-form #js {}
        path "/model/pictures"]
    (set! (.-init $scope) #(.reset $scope))
    (set! (.-reset $scope) #(set! (.-album $scope) default-form))

    (set! (.-submit $scope)
          (fn []
            (timbre/info "Submitting picture form")
            ;; TODO: Use the model
            (let [params (.-album $scope)
                  form-data (js/FormData. params)
                  options #js {:transformRequest (.-identity js/angular)
                               :headers #js {"Content-Type" js/undefined}}]

              (doseq [o (.-files $scope)]
                (.append form-data "files[]" (.-lfFile o)))

              (.append form-data "album" (.-_id (.-item $scope)))

              (.forEach js/angular params
                        (fn [k v] (.append form-data k v)))

              (.post $http path form-data options))))

    (.init $scope)))

(def.controller jiksnu.NewPostController
  [$scope $rootScope geolocation app pageService subpageService $filter Streams Users]
  (timbre/debug "Loading New Post Controller")
  (helpers/init-subpage $scope app Users "streams")
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
  (set! (.-visible $scope) (fn [] (and (.enabled $scope) app.user)))
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
  (set! (.-getLocation $scope)
        (fn []
          (.. geolocation
              (getLocation)
              (then (fn [data]
                      (let [geo (.. $scope -activity -geo)
                            coords (.-coords data)]
                        (set! (.-latitude geo) (.-latitude coords))
                        (set! (.-longitude geo) (.-longitude coords))))
                    (fn [data] (timbre/errorf "Location error: %s" data))))))
  (set! (.-reset $scope)
        (fn []
          (set! (.-activity $scope) (.-defaultForm $scope))
          (set! (.. $scope -activity -streams) #js [])))
  (set! (.-submit $scope)
        (fn []
          (js/console.info "Scope: " $scope)
          (let [activity (.-activity $scope)
                pictures (map #(.-lfFile %) (.-files $scope))]
            (-> (.post app activity pictures)
                (.then (fn []
                         (.reset $scope)
                         (.toggle $scope)
                         (.refresh app)))))))
  (set! (.-toggle $scope)
        (fn []
          (timbre/debug "Toggling New Post form")
          (set! (.. $scope -form -shown) (not (.. $scope -form -shown)))
          (when (.. $scope -form -shown)
            (.getLocation $scope)
            (.fetchStreams $scope))))
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

(def.controller jiksnu.SettingsPageController [])

(def.controller jiksnu.ShowActivityController
  [$scope $stateParams Activities app $rootScope]
  (set! (.-app $scope) app)

  (set! (.-likeActivity $scope)
        (fn [activity]
          (-> app
              (.invokeAction "activity" "like" (.-id $scope))
              (.then (fn [] (.refresh $scope))))))

  (helpers/init-item $scope $stateParams app Activities))

(def.controller jiksnu.ShowAlbumController
  [$scope $stateParams app Albums]
  (set! (.-loaded $scope) false)
  (helpers/init-item $scope $stateParams app Albums))

(def.controller jiksnu.ShowAlbumMinimalController
  [$scope $stateParams app Albums]
  (set! (.-loaded $scope) false)
  (helpers/init-item $scope $stateParams app Albums))

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

(def.controller jiksnu.ShowPictureController
  [$scope $stateParams app Pictures]
  (helpers/init-item $scope $stateParams app Pictures))

(def.controller jiksnu.ShowPictureMinimalController
  [$scope $stateParams app Pictures]
  (helpers/init-item $scope $stateParams app Pictures))

(def.controller jiksnu.ShowRequestTokenController
  [$scope $http $stateParams app RequestTokens]
  (helpers/init-item $scope $stateParams app RequestTokens))

(def.controller jiksnu.ShowServiceController
  [$scope $http $stateParams app Services]
  (helpers/init-item $scope $stateParams app Services))

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

(def.controller jiksnu.SidenavController
  [$scope app]
  (let [route-data
        [["Home"          "home"]
         ["Activities"    "indexActivities"]
         #_["Domains"       "indexDomains"]
         ["Groups"        "indexGroups"]
         ["Likes"         "indexLikes"]
         ["Albums"        "indexAlbums"]
         ["Notifications" "indexNotifications"]
         ["Pictures"      "indexPictures"]
         #_["Services"      "indexServices"]
         #_["Streams"       "indexStreams"]
         ["Users"         "indexUsers"]
         #_["Settings"      "settingsPage"]
         #_["Profile"       "profile"]]]

    (set! (.-app $scope) app)

    (set! (.-items $scope)
          (clj->js
           (map (fn [[label ref]] {:label label :ref ref}) route-data)))))

(def.controller jiksnu.SubpageController
  [$scope subpageService $rootScope]
  (set! (.-loaded $scope) false)
  (if-let [subpage (.-subpage $scope)]
    (do
      ;; (timbre/debug "initialize subpage controller" subpage)
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
                (let [model-name (.. item -constructor -name)]
                  (set! (.-item $scope) item)
                  (set! (.-loaded $scope) false)
                  (set! (.-loading $scope) true)
                  (timbre/debugf "Refreshing subpage: %s(%s)=>%s"
                                 model-name (.-_id item) subpage)
                  (-> (.fetch subpageService item subpage)
                      (.then (fn [page]
                               (set! (.-errored $scope) false)
                               (set! (.-loaded $scope) true)
                               (set! (.-loading $scope) false)
                               (set! (.-page $scope) page)
                               page)
                             (fn [page]
                               (timbre/errorf "Failed to load subpage. %s(%s)=>%s"
                                              model-name (.-_id item) subpage)
                               (set! (.-errored $scope) true)
                               (set! (.-loading $scope) false)
                               page))))
                (throw (str "parent item not bound for subpage: " subpage)))))
      (.refresh $scope))
    (throw "Subpage not specified")))
