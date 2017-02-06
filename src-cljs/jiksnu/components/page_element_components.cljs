(ns jiksnu.components.page-element-components
  (:require [jiksnu.app :refer [jiksnu]]
            [jiksnu.helpers :as helpers]
            [jiksnu.registry :as registry]
            jiksnu.services
            [taoensso.timbre :as timbre])
  (:use-macros [gyr.core :only [def.controller def.directive]]))

(.controller jiksnu "AppController" #js [(fn [])])

(defn AsModelController
  [$scope DS]
  (let [collection-name (.-model $scope)]
    (set! (.-init $scope)
          (fn [id]
            (timbre/debugf "init as model %s(%s)" collection-name id)
            (.bindOne DS collection-name id $scope "item")
            (.find DS collection-name id)))
    (.init $scope (.-id $scope))))

(.component
 jiksnu "asModel"
 #js {:bindings #js {:id "<" :model "@"}
      :controller #js ["$scope" "DS" AsModelController]
      :template "<span ng-transclude></span>"
      :transclude true})

(defn DebugController
  [$scope $filter app]
  (set! (.-visible $scope) #(.. app -data -debug))

  (set! (.-formattedCode $scope)
        #(($filter "json") (.-expr $scope))))

(.component
 jiksnu "debug"
 #js {:scope #js {:expr "=expr"
                  :exprText "@expr"}
      :templateUrl "/templates/debug"
      :controller #js ["$scope" "$filter" "app" DebugController]})

(defn DisplayAvatarController
  [$ctrl $scope Users]
  (set! (.-$onChanges $ctrl) #(when (.-id %) (.init $scope)))
  (set! (.-init $scope)
        (fn []
          (set! (.-size $scope) (or (.-size $ctrl) 32))
          (when-let [id (.-id $ctrl)]
            (when (seq id)
              (timbre/debugf "Displaying avatar for %s" id)
              (.bindOne Users id $scope "user")
              (.find Users id)))))
  (.init $scope))

(.component
 jiksnu "displayAvatar"
 #js {:bindings #js {:id "@" :size "@"}
      :controller #js ["$scope" "Users"
                       (fn [$scope Users]
                         (this-as $ctrl (DisplayAvatarController $ctrl $scope Users)))]
      :templateUrl "/templates/display-avatar"})

(defn FollowButtonController
  [$scope app $q $rootScope Subscriptions]
  (set! (.-app $scope) app)
  (set! (.-loaded $scope) false)

  (set! (.-isActor $scope)
        (fn []
          (set! (.-authenticated $scope)
                (some-> app .getUserId (= (some-> $scope .-item .-_id))))))

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
                 (.. app
                     (getUser)
                     (then #(some-> % .getFollowing))
                     (then (fn [page]
                             (when page
                               (->> (.-items page)
                                    (map (.-find Subscriptions))
                                    clj->js
                                    (.all $q)))))
                     (then (fn [subscriptions]
                             (some #(#{user-id} (.-to %)) subscriptions)))
                     (then resolve)))
               (reject))))))

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

(.controller
 jiksnu "FollowButtonController"
 #js ["$scope" "app" "$q" "$rootScope" "Subscriptions" FollowButtonController])

(.component
 jiksnu "followButton"
 #js {:controller "FollowButtonController"
      :bindings #js {:item "<"}
      :templateUrl "/templates/follow-button"})

(defn swagger-url
  [protocol hostname port]
  (let [secure? (= protocol "https")
        default-port? (or (and secure?       (= port 443))
                          (and (not secure?) (= port 80)))]
    (str protocol "://" hostname
         (when-not default-port? (str ":" port))
         "/api-docs.json")))

(defn MainLayoutController
  [$location $mdSidenav $scope app]
  (let []
    (set! (.-getSwaggerUrl $scope)
          (fn []
            (let [protocol (.protocol $location)
                  hostname (.host $location)
                  port (.port $location)]
              (swagger-url protocol hostname port))))
    (set! (.-apiUrl $scope) (str "/vendor/swagger-ui/dist/index.html?url=" (.getSwaggerUrl $scope)))
    (set! (.-$mdSidenav $scope) $mdSidenav)
    (set! (.-logout $scope) (.-logout app))))

(.component
 jiksnu "mainLayout"
 #js {:templateUrl "/templates/main-layout"
      :controller #js ["$location" "$mdSidenav" "$scope" "app" MainLayoutController]})

(defn NavBarController
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
            (set! (.-app $scope) d)
            (-> (.getUser app)
                (.then (fn [user]
                         (set! (.-user app) user)))))))

  (set! (.-toggleSidenav $scope) (fn [] (.toggle ($mdSidenav "left"))))

  (.$watch $scope #(.-data app) (.-init $scope))

  (-> (.fetchStatus app)
      (.then (fn [] (set! (.-loaded $scope) true)))))

(.controller
 jiksnu "NavBarController"
 #js ["$mdSidenav" "$rootScope" "$scope" "$state" "app" "hotkeys" NavBarController])

(.component
 jiksnu "navBar"
 #js {:controller "NavBarController"
      :templateUrl "/templates/navbar-section"})

(defn SidenavController
  [$scope app]

  (set! (.-logout $scope) (.-logout app))
  (set! (.-app $scope) app)

  (set! (.-items $scope)
        (clj->js
         (map (fn [[label ref]] {:label label :ref ref}) registry/sidenav-data))))

(.component
 jiksnu "sidenav"
 #js {:templateUrl "/templates/sidenav"
      :controller #js ["$scope" "app" SidenavController]})

(.component
 jiksnu "spinner"
 #js {:templateUrl "/templates/spinner"})

(def.controller jiksnu.SubpageController
  [$scope subpageService $rootScope]
  (set! (.-loaded $scope) false)
  (if-let [subpage (.-subpage $scope)]
    (do
      (set! (.-refresh $scope) (fn [] (.init $scope (.-item $scope))))
      (.$on $scope "refresh-page" (fn [] (.refresh $scope)))

      (set! (.-init $scope)
            (fn [item]
              (if item
                (let [model-name (.. item -constructor -name)
                      id (.-_id item)]
                  (set! (.-item $scope) item)
                  (set! (.-loaded $scope) false)
                  (set! (.-loading $scope) true)
                  (timbre/debugf "Refreshing subpage: %s(%s)=>%s" model-name id subpage)
                  (-> (.fetch subpageService item subpage)
                      (.then
                       (fn [page]
                         (set! (.-errored $scope) false)
                         (set! (.-loaded $scope) true)
                         (set! (.-loading $scope) false)
                         (set! (.-page $scope) page)
                         page)
                       (fn [page]
                         (timbre/errorf "Failed to load subpage. %s(%s)=>%s" model-name id subpage)
                         (set! (.-errored $scope) true)
                         (set! (.-loading $scope) false)
                         page)))))))
      (.refresh $scope))
    (throw "Subpage not specified")))

(def.directive jiksnu.subpage []
  #js {:scope #js {:subpage "@name" :item "=item"}
       :template "<div ng-transclude></div>"
       :transclude true
       :controller "SubpageController"})
