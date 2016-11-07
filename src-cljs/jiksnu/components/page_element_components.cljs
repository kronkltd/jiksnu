(ns jiksnu.components.page-element-components
  (:require [jiksnu.app :refer [jiksnu]]
            [jiksnu.helpers :as helpers]
            [taoensso.timbre :as timbre])
  (:use-macros [gyr.core :only [def.controller def.directive]]))

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

(def.directive jiksnu.asModel []
  #js
  {:controller "AsModelController"
   :template "<span ng-transclude></span>"
   :scope #js {:id "@" :model "@"}
   :transclude true})

(def.controller jiksnu.DebugController [$scope $filter app]
  (set! (.-visible $scope) #(.. app -data -debug))

  (set! (.-formattedCode $scope)
        #(($filter "json") (.-expr $scope))))

(def.directive jiksnu.debug []
  #js
  {:controller "DebugController"
   :scope #js {:expr "=expr"
               :exprText "@expr"}
   :templateUrl "/templates/debug"})

(def.controller jiksnu.DisplayAvatarController
  [$scope Users]
  (set! (.-init $scope)
        (fn []
          (js/console.log "Scope" $scope)
          (when-let [id (.-id $scope)]
            ;; (timbre/debugf "Displaying avatar for %s" id)
            (set! (.-size $scope) (or (.-size $scope) 32))
            (.bindOne Users id $scope "user")
            (.find Users id))))
  (.init $scope))

(def.directive jiksnu.displayAvatar []
  #js
  {:controller "DisplayAvatarController"
   :scope #js {:id "@" :size "@"}
   :templateUrl "/templates/display-avatar"})

(def.controller jiksnu.FollowButtonController
  [$scope app $q $rootScope Subscriptions]
  (set! (.-app $scope) app)
  (set! (.-loaded $scope) false)

  (set! (.-isActor $scope)
        (fn []
          (if-let [item-id (.-_id (.-item $scope))]
            (set! (.-authenticated $scope)
                  (or (-> app .getUserId (= item-id)) false))
            (throw "No item bound to scope"))))

  (set! (.-init $scope)
        (fn []
          (let [actor (.isActor $scope)]
            (set! (.-actor $scope) actor)
            (when-not actor
              (-> (.isFollowing $scope)
                  (.then (fn [following]
                           (when following (.log following "info"))
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
                 #_
                 (timbre/debug "Checking if following")
                 (.. app
                     (getUser)
                     (then #(some-> % .getFollowing))
                     (then (fn [page]
                             #_
                             (timbre/debug "Got page")
                             (when page
                               (->> (.-items page)
                                    (map (.-find Subscriptions))
                                    clj->js
                                    (.all $q)))))
                     (then (fn [subscriptions]
                             #_
                             (timbre/debug "got subscriptions")
                             (some #(#{user-id} (.-to %)) subscriptions)))
                     (then resolve)))
               (do
                 #_
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
                         (.$broadcast $rootScope helpers/refresh-followers)))))))

  (.$watch $scope
           (fn [] (.-data app))
           (fn [data old-data] (.init $scope)))
  (.$on $scope helpers/refresh-followers (.-init $scope)))

(def.directive jiksnu.followButton
  []
  #js
  {:controller "FollowButtonController"
   :scope #js {:item "="}
   :templateUrl "/templates/follow-button"})

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

(def.directive jiksnu.mainLayout []
  #js
  {:templateUrl "/templates/main-layout"
   :controller "MainLayoutController"})

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
            #_
            (timbre/debug "Running navbarcontroller watcher")
            (set! (.-app $scope) d)
            (-> (.getUser app)
                (.then (fn [user]
                         #_
                         (timbre/debug "setting app user")
                         (set! (.-user app) user)))))))

  (set! (.-toggleSidenav $scope) (fn [] (.toggle ($mdSidenav "left"))))

  (.$watch $scope #(.-data app) (.-init $scope))

  (-> (.fetchStatus app)
      (.then (fn [] (set! (.-loaded $scope) true)))))

(def.directive jiksnu.navBar []
  #js
  {:controller "NavBarController"
   :scope true
   :templateUrl "/templates/navbar-section"})

(def.controller jiksnu.SidenavController
  [$scope app]
  (let [route-data
        [["Home"          "home"]
         ["Activities"    "indexActivities"]
         #_
         ["Domains"       "indexDomains"]
         ["Groups"        "indexGroups"]
         ["Likes"         "indexLikes"]
         ["Albums"        "indexAlbums"]
         ["Notifications" "indexNotifications"]
         ["Pictures"      "indexPictures"]
         #_
         ["Services"      "indexServices"]
         #_
         ["Streams"       "indexStreams"]
         ["Users"         "indexUsers"]
         #_
         ["Settings"      "settingsPage"]
         #_
         ["Profile"       "profile"]]]

    (set! (.-app $scope) app)

    (set! (.-items $scope)
          (clj->js
           (map (fn [[label ref]] {:label label :ref ref}) route-data)))))

(def.directive jiksnu.sidenav []
  #js
  {:templateUrl "/templates/sidenav"
   :controller "SidenavController"})

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

(def.directive jiksnu.subpage []
  #js
  {:scope #js {:subpage "@name" :item "=item"}
   :template "<div ng-transclude></div>"
   :transclude true
   :controller "SubpageController"})
