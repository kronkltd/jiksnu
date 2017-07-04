(ns jiksnu.app.components.show-components
  (:require [inflections.core :as inf]
            [jiksnu.app :refer [jiksnu]]
            [jiksnu.app.helpers :as helpers]
            [jiksnu.app.protocols :as proto]
            [taoensso.timbre :as timbre]))

(defn item-directive
  [module model-name controller]
  (let [controller-name (str "Show" (inf/camel-case model-name) "Controller")
        component-name (str "show" (inf/camel-case model-name))
        template-path (str "/templates/show-" (inf/dasherize model-name))
        component-options #js {:templateUrl template-path
                               :controller controller
                               :bindings #js {:id "@" :item "="}}]
    (.controller module controller-name controller)
    (.component  module component-name component-options)))

(defn ShowActivityController
  [$scope app Activities]
  (set! $scope.likeActivity
        (fn [activity]
          (-> (proto/invoke-action app "activity" "like" $scope.id)
              (.then (fn [] (.refresh $scope))))))
  (this-as $ctrl (helpers/init-item $ctrl $scope app Activities)))

(defn ShowAlbumController
  [$scope app Albums]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Albums)))

(defn ShowAlbumMinimalController
  [$scope app Albums]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Albums)))

(defn ShowClientController
  [$scope app Clients]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Clients)))

(defn ShowClientMinimalController
  [$scope app Clients]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Clients)))

(defn ShowConversationController
  [$scope app Conversations]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Conversations)))

(defn ShowDomainController
  [$scope app Domains]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Domains)))

(defn ShowFollowersMinimalController
  [$scope app Subscriptions]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Subscriptions)))

(defn ShowFollowingMinimalController
  [$scope app Subscriptions]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Subscriptions)))

(defn ShowGroupController
  [$scope app Groups]
  (set! $scope.join
        (fn []
          (timbre/info "Joining group")
          (let [id (.-_id (.-item $scope))]
            (proto/invoke-action app "group" "join" id))))
  (this-as $ctrl (helpers/init-item $ctrl $scope app Groups)))

(defn ShowGroupMinimalController
  [$scope app Groups]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Groups)))

(defn ShowGroupMembershipController
  [$scope app GroupMemberships]
  (this-as $ctrl (helpers/init-item $ctrl $scope app GroupMemberships)))

(defn ShowGroupMembershipMinimalController
  [$scope app GroupMemberships]
  (this-as $ctrl (helpers/init-item $ctrl $scope app GroupMemberships)))

(defn ShowLikeController
  [$scope app Likes]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Likes)))

(defn ShowLikedByController
  [$scope app Likes]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Likes)))

(defn ShowNotificationController
  [$scope app Notifications]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Notifications)))

(defn ShowPictureController
  [$scope app Pictures]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Pictures)))

(defn ShowPictureMinimalController
  [$scope app Pictures]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Pictures)))

(defn ShowRequestTokenController
  [$scope app RequestTokens]
  (this-as $ctrl (helpers/init-item $ctrl $scope app RequestTokens)))

(defn ShowServiceController
  [$scope app Services]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Services)))

(defn ShowStreamController
  [$scope app Streams]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Streams)))

(defn ShowStreamMinimalController
  [$scope app Streams]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Streams))
  (set! $scope.toggle
        (fn []
          (let [shown? (not (.-formShown $scope))]
            (set! $scope.formShown shown?)
            (set! $scope.btnLabel  (if shown? "-" "+"))))))

(defn ShowSubscriptionController
  [$scope app Subscriptions]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Subscriptions)))

(defn ShowUserController
  [$scope app Users]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Users)))

(defn ShowUserMinimalController
  [$scope app Users]
  (this-as $ctrl (helpers/init-item $ctrl $scope app Users)))

(item-directive
 jiksnu "activity"
 #js ["$scope" "app" "Activities" ShowActivityController])

(item-directive
 jiksnu "album"
 #js ["$scope" "app" "Albums" ShowAlbumController])

(item-directive
 jiksnu "album-minimal"
 #js ["$scope" "app" "Albums" ShowAlbumMinimalController])

(item-directive
 jiksnu "client"
 #js ["$scope" "app" "Clients" ShowClientController])

(item-directive
 jiksnu "client-minimal"
 #js ["$scope" "app" "Clients"  ShowClientMinimalController])

(item-directive
 jiksnu "conversation"
 #js ["$scope" "app" "Conversations"  ShowConversationController])

(item-directive
 jiksnu "domain"
 #js ["$scope" "app" "Domains"  ShowDomainController])

(item-directive
 jiksnu "followers-minimal"
 #js ["$scope" "app" "Subscriptions"  ShowFollowersMinimalController])

(item-directive
 jiksnu "following-minimal"
 #js ["$scope" "app" "Subscriptions"  ShowFollowingMinimalController])

(item-directive
 jiksnu "group"
 #js ["$scope" "app" "Groups"  ShowGroupController])

(item-directive
 jiksnu "group-minimal"
 #js ["$scope" "app" "Groups"  ShowGroupMinimalController])

(item-directive
 jiksnu "group-membership"
 #js ["$scope" "app" "GroupMemberships"  ShowGroupMembershipController])

(item-directive
 jiksnu "group-membership-minimal"
 #js ["$scope" "app" "GroupMemberships"  ShowGroupMembershipMinimalController])

(item-directive
 jiksnu "like"
 #js ["$scope" "app" "Likes" ShowLikeController])

(item-directive
 jiksnu "liked-by"
 #js ["$scope" "app" "Likes" ShowLikedByController])

(item-directive
 jiksnu "notification"
 #js ["$scope" "app" "Notifications"  ShowNotificationController])

(item-directive
 jiksnu "picture"
 #js ["$scope" "app" "Pictures"  ShowPictureController])

(item-directive
 jiksnu "picture-minimal"
 #js ["$scope" "app" "Pictures" ShowPictureMinimalController])

(item-directive
 jiksnu "request-token"
 #js ["$scope" "app" "RequestToken" ShowRequestTokenController])

(item-directive
 jiksnu "service"
 #js ["$scope" "app" "Services" ShowServiceController])

(item-directive
 jiksnu "stream"
 #js ["$scope" "app" "Streams" ShowStreamController])

(item-directive
 jiksnu "stream-minimal"
 #js ["$scope" "app" "Streams" ShowStreamMinimalController])

(item-directive
 jiksnu "subscription"
 #js ["$scope" "app" "Subscriptions"  ShowSubscriptionController])

(item-directive
 jiksnu "user"
 #js ["$scope" "app" "Users" ShowUserController])

(item-directive
 jiksnu "user-minimal"
 #js ["$scope" "app" "Users" ShowUserMinimalController])
