(ns jiksnu.components.show-components
  (:require [jiksnu.app :refer [jiksnu]]
            [jiksnu.helpers :as helpers]
            [jiksnu.macros :refer-macros [item-directive]]
            [taoensso.timbre :as timbre])
  (:use-macros [gyr.core :only [def.controller def.directive]]))

(defn ShowActivityController
  [$scope $stateParams app Activities]
  (set! (.-app $scope) app)

  (set! (.-likeActivity $scope)
        (fn [activity]
          (-> app
              (.invokeAction "activity" "like" (.-id $scope))
              (.then (fn [] (.refresh $scope))))))

  (helpers/init-item $scope $stateParams app Activities))

(set! (.-$inject ShowActivityController) #js ["$scope" "$stateParams" "app" "Activities"])
(.controller jiksnu "ShowActivityController" ShowActivityController)
(item-directive "Activity"               "activity")

(defn ShowAlbumController
  [$scope $stateParams app Albums]
  (set! (.-loaded $scope) false)
  (helpers/init-item $scope $stateParams app Albums))

(set! (.-$inject ShowAlbumController) #js ["$scope" "$stateParams" "app" "Albums"])
(.controller jiksnu "ShowAlbumController" ShowAlbumController)
(item-directive "Album"                  "album")

(defn ShowAlbumMinimalController
  [$scope $stateParams app Albums]
  (set! (.-loaded $scope) false)
  (helpers/init-item $scope $stateParams app Albums))

(set! (.-$inject ShowAlbumMinimalController) #js ["$scope" "$stateParams" "app" "Albums"])
(.controller jiksnu "ShowAlbumMinimalController" ShowAlbumMinimalController)
(item-directive "AlbumMinimal"           "album-minimal")

(defn ShowClientController
  [$scope $stateParams app Clients]
  (helpers/init-item $scope $stateParams app Clients))

(set! (.-$inject ShowClientController) #js ["$scope" "$stateParams" "app"  "Clients"])
(.controller jiksnu "ShowClientController" ShowClientController)
(item-directive "Client"                 "client")

(defn ShowClientMinimalController
  [$scope $stateParams app Clients]
  (helpers/init-item $scope $stateParams app Clients))

(set! (.-$inject ShowClientMinimalController) #js ["$scope" "$stateParams" "app" "Clients"])
(.controller jiksnu "ShowClientMinimalController" ShowClientMinimalController)
(item-directive "ClientMinimal"          "client-minimal")

(defn ShowConversationController
  [$scope $stateParams app Conversations]
  (helpers/init-item $scope $stateParams app Conversations)
  (set! (.-app $scope) app))

(set! (.-$inject ShowConversationController) #js ["$scope" "$stateParams" "app" "Conversations"])
(.controller jiksnu "ShowConversationController" ShowConversationController)
(item-directive "Conversation"           "conversation")

(defn ShowDomainController
  [$scope $stateParams app Domains]
  (set! (.-loaded $scope) false)
  (helpers/init-item $scope $stateParams app Domains))

(set! (.-$inject ShowDomainController) #js ["$scope" "$stateParams" "Albums" "app"])
(.controller jiksnu "ShowDomainController" ShowDomainController)
(item-directive "Domain"           "domain")

(defn ShowFollowersMinimalController
  [$scope $stateParams app Subscriptions]
  (helpers/init-item $scope $stateParams app Subscriptions))

(item-directive "FollowersMinimal"       "followers-minimal")

(defn ShowFollowingMinimalController
  [$scope $stateParams app Subscriptions]
  (helpers/init-item $scope $stateParams app Subscriptions))

(item-directive "FollowingMinimal"       "following-minimal")

(defn ShowGroupController
  [$scope $stateParams app Groups]
  (timbre/debug "loading ShowGroupController")
  (set! (.-join $scope)
        (fn []
          (timbre/info "Joining group")
          (let [id (.-_id (.-item $scope))]
            (.invokeAction app "group" "join" id))))
  (helpers/init-item $scope $stateParams app Groups))

(item-directive "Group"                  "group")

(defn ShowGroupMinimalController
  [$scope $stateParams app Groups]
  (helpers/init-item $scope $stateParams app Groups))

(item-directive "GroupMinimal"           "group-minimal")

(defn ShowGroupMembershipController
  [$scope $stateParams app GroupMemberships]
  (helpers/init-item $scope $stateParams app GroupMemberships))

(item-directive "GroupMembership" "group-membership")

(defn ShowGroupMembershipMinimalController
  [$scope $stateParams app GroupMemberships]
  (helpers/init-item $scope $stateParams app GroupMemberships))

(item-directive "GroupMembershipMinimal" "group-membership-minimal")

(defn ShowLikeController
  [$scope $stateParams app Likes]
  (helpers/init-item $scope $stateParams app Likes))

(item-directive "Like"                   "like")

(defn ShowLikedByController
  [$scope $stateParams app Likes]
  (helpers/init-item $scope $stateParams app Likes))

(item-directive "LikedBy"                "liked-by")

(defn ShowNotificationController
  [$scope $stateParams app Notifications]
  (helpers/init-item $scope $stateParams app Notifications))

(item-directive "Notification"           "notification")

(defn ShowPictureController
  [$scope $stateParams app Pictures]
  (helpers/init-item $scope $stateParams app Pictures))

(item-directive "Picture"                "picture")

(defn ShowPictureMinimalController
  [$scope $stateParams app Pictures]
  (helpers/init-item $scope $stateParams app Pictures))

(item-directive "PictureMinimal"         "picture-minimal")

(defn ShowRequestTokenController
  [$scope $stateParams app RequestTokens]
  (helpers/init-item $scope $stateParams app RequestTokens))

(item-directive "RequestToken"           "request-token")

(defn ShowServiceController
  [$scope $stateParams app Services]
  (helpers/init-item $scope $stateParams app Services))

(item-directive "Service"                "service")

(defn ShowStreamController
  [$scope $stateParams app Streams]
  (helpers/init-item $scope $stateParams app Streams))

(defn ShowStreamMinimalController
  [$scope $stateParams app Streams]
  (helpers/init-item $scope $stateParams app Streams)
  (set! (.-toggle $scope)
        (fn []
          (let [shown? (not (.-formShown $scope))]
            (set! (.-formShown $scope) shown?)
            (set! (.-btnLabel $scope) (if shown? "-" "+"))))))

(item-directive "StreamMinimal"          "stream-minimal")

(defn ShowSubscriptionController
  [$scope $stateParams app Subscriptions]
  (helpers/init-item $scope $stateParams app Subscriptions))

(item-directive "Subscription"           "subscription")

(defn ShowUserController
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

(item-directive "User"                   "user")

(defn ShowUserMinimalController
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

(item-directive "UserMinimal"            "user-minimal")
