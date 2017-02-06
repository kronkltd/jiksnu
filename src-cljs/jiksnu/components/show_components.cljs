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

(set! (.-$inject ShowDomainController) #js ["$scope" "$stateParams" "app" "Domains"])
(.controller jiksnu "ShowDomainController" ShowDomainController)
(item-directive "Domain"           "domain")

(defn ShowFollowersMinimalController
  [$scope $stateParams app Subscriptions]
  (helpers/init-item $scope $stateParams app Subscriptions))

(set! (.-$inject ShowFollowersMinimalController) #js ["$scope" "$stateParams" "app" "Subscriptions"])
(.controller jiksnu "ShowFollowersMinimalController" ShowFollowersMinimalController)
(item-directive "FollowersMinimal"       "followers-minimal")

(defn ShowFollowingMinimalController
  [$scope $stateParams app Subscriptions]
  (helpers/init-item $scope $stateParams app Subscriptions))

(set! (.-$inject ShowFollowingMinimalController) #js ["$scope" "$stateParams" "app" "Subscriptions"])
(.controller jiksnu "ShowFollowingMinimalController" ShowFollowingMinimalController)
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

(set! (.-$inject ShowGroupController) #js ["$scope" "$stateParams" "app" "Groups"])
(.controller jiksnu "ShowGroupController" ShowGroupController)
(item-directive "Group"                  "group")

(defn ShowGroupMinimalController
  [$scope $stateParams app Groups]
  (helpers/init-item $scope $stateParams app Groups))

(set! (.-$inject ShowGroupMinimalController) #js ["$scope" "$stateParams" "app" "Groups"])
(.controller jiksnu "ShowGroupMinimalController" ShowGroupMinimalController)
(item-directive "GroupMinimal"           "group-minimal")

(defn ShowGroupMembershipController
  [$scope $stateParams app GroupMemberships]
  (helpers/init-item $scope $stateParams app GroupMemberships))

(set! (.-$inject ShowGroupMembershipController) #js ["$scope" "$stateParams" "app" "GroupMemberships"])
(.controller jiksnu "ShowGroupMembershipController" ShowGroupMembershipController)
(item-directive "GroupMembership" "group-membership")

(defn ShowGroupMembershipMinimalController
  [$scope $stateParams app GroupMemberships]
  (helpers/init-item $scope $stateParams app GroupMemberships))

(set! (.-$inject ShowGroupMembershipMinimalController) #js ["$scope" "$stateParams" "app" "GroupMemberships"])
(.controller jiksnu "ShowGroupMembershipMinimalController" ShowGroupMembershipMinimalController)
(item-directive "GroupMembershipMinimal" "group-membership-minimal")

(defn ShowLikeController
  [$scope $stateParams app Likes]
  (helpers/init-item $scope $stateParams app Likes))

(set! (.-$inject ShowLikeController) #js ["$scope" "$stateParams" "app" "Likes"])
(.controller jiksnu "ShowLikeController" ShowLikeController)
(item-directive "Like"                   "like")

(defn ShowLikedByController
  [$scope $stateParams app Likes]
  (helpers/init-item $scope $stateParams app Likes))

(set! (.-$inject ShowLikedByController) #js ["$scope" "$stateParams" "app" "Likes"])
(.controller jiksnu "ShowLikedByController" ShowLikedByController)
(item-directive "LikedBy"                "liked-by")

(defn ShowNotificationController
  [$scope $stateParams app Notifications]
  (helpers/init-item $scope $stateParams app Notifications))

(set! (.-$inject ShowNotificationController) #js ["$scope" "$stateParams" "app" "Notifications"])
(.controller jiksnu "ShowNotificationController" ShowNotificationController)
(item-directive "Notification"           "notification")

(defn ShowPictureController
  [$scope $stateParams app Pictures]
  (helpers/init-item $scope $stateParams app Pictures))

(set! (.-$inject ShowPictureController) #js ["$scope" "$stateParams" "app" "Pictures"])
(.controller jiksnu "ShowPictureController" ShowPictureController)
(item-directive "Picture"                "picture")

(defn ShowPictureMinimalController
  [$scope $stateParams app Pictures]
  (helpers/init-item $scope $stateParams app Pictures))

(set! (.-$inject ShowPictureMinimalController) #js ["$scope" "$stateParams" "app" "Pictures"])
(.controller jiksnu "ShowPictureMinimalController" ShowPictureMinimalController)
(item-directive "PictureMinimal"         "picture-minimal")

(defn ShowRequestTokenController
  [$scope $stateParams app RequestTokens]
  (helpers/init-item $scope $stateParams app RequestTokens))

(set! (.-$inject ShowRequestTokenController) #js ["$scope" "$stateParams" "app" "RequestToken"])
(.controller jiksnu "ShowRequestTokenController" ShowRequestTokenController)
(item-directive "RequestToken"           "request-token")

(defn ShowServiceController
  [$scope $stateParams app Services]
  (helpers/init-item $scope $stateParams app Services))

(set! (.-$inject ShowServiceController) #js ["$scope" "$stateParams" "app" "Services"])
(.controller jiksnu "ShowServiceController" ShowServiceController)
(item-directive "Service"                "service")

(defn ShowStreamController
  [$scope $stateParams app Streams]
  (helpers/init-item $scope $stateParams app Streams))

(set! (.-$inject ShowStreamController) #js ["$scope" "$stateParams" "app" "Streams"])
(.controller jiksnu "ShowStreamController" ShowStreamController)
(item-directive "Stream"          "stream")

(defn ShowStreamMinimalController
  [$scope $stateParams app Streams]
  (helpers/init-item $scope $stateParams app Streams)
  (set! (.-toggle $scope)
        (fn []
          (let [shown? (not (.-formShown $scope))]
            (set! (.-formShown $scope) shown?)
            (set! (.-btnLabel $scope) (if shown? "-" "+"))))))

(set! (.-$inject ShowStreamMinimalController) #js ["$scope" "$stateParams" "app" "Streams"])
(.controller jiksnu "ShowStreamMinimalController" ShowStreamMinimalController)
(item-directive "StreamMinimal"          "stream-minimal")

(defn ShowSubscriptionController
  [$scope $stateParams app Subscriptions]
  (helpers/init-item $scope $stateParams app Subscriptions))

(set! (.-$inject ShowSubscriptionController) #js ["$scope" "$stateParams" "app" "Subscriptions"])
(.controller jiksnu "ShowSubscriptionController" ShowSubscriptionController)
(item-directive "Subscription"           "subscription")

(defn ShowUserController
  [$scope $stateParams app Users]
  (helpers/init-item $scope $stateParams app Users))

(set! (.-$inject ShowUserController) #js ["$scope" "$stateParams" "app" "Users"])
(.controller jiksnu "ShowUserController" ShowUserController)
(item-directive "User"                   "user")

(defn ShowUserMinimalController
  [$scope $stateParams app Users]
  (helpers/init-item $scope $stateParams app Users))

(set! (.-$inject ShowUserMinimalController) #js ["$scope" "$stateParams" "app" "Users"])
(.controller jiksnu "ShowUserMinimalController" ShowUserMinimalController)
(item-directive "UserMinimal"            "user-minimal")
