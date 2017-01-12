(ns jiksnu.components.show-components
  (:require jiksnu.app
            [jiksnu.helpers :as helpers]
            [taoensso.timbre :as timbre])
  (:use-macros [gyr.core :only [def.controller]]))

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
