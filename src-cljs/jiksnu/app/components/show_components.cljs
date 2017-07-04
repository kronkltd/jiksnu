(ns jiksnu.app.components.show-components
  (:require [inflections.core :as inf]
            [jiksnu.app :refer [jiksnu]]
            [jiksnu.app.helpers :as helpers]
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
  [$scope $stateParams app Activities]
  (set! (.-likeActivity $scope)
        (fn [activity]
          (-> app
              (.invokeAction "activity" "like" (.-id $scope))
              (.then (fn [] (.refresh $scope))))))
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Activities)))

(defn ShowAlbumController
  [$scope $stateParams app Albums]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Albums)))

(defn ShowAlbumMinimalController
  [$scope $stateParams app Albums]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Albums)))

(defn ShowClientController
  [$scope $stateParams app Clients]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Clients)))

(defn ShowClientMinimalController
  [$scope $stateParams app Clients]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Clients)))

(defn ShowConversationController
  [$scope $stateParams app Conversations]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Conversations)))

(defn ShowDomainController
  [$scope $stateParams app Domains]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Domains)))

(defn ShowFollowersMinimalController
  [$scope $stateParams app Subscriptions]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Subscriptions)))

(defn ShowFollowingMinimalController
  [$scope $stateParams app Subscriptions]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Subscriptions)))

(defn ShowGroupController
  [$scope $stateParams app Groups]
  (set! (.-join $scope)
        (fn []
          (timbre/info "Joining group")
          (let [id (.-_id (.-item $scope))]
            (.invokeAction app "group" "join" id))))
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Groups)))

(defn ShowGroupMinimalController
  [$scope $stateParams app Groups]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Groups)))

(defn ShowGroupMembershipController
  [$scope $stateParams app GroupMemberships]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app GroupMemberships)))

(defn ShowGroupMembershipMinimalController
  [$scope $stateParams app GroupMemberships]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app GroupMemberships)))

(defn ShowLikeController
  [$scope $stateParams app Likes]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Likes)))

(defn ShowLikedByController
  [$scope $stateParams app Likes]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Likes)))

(defn ShowNotificationController
  [$scope $stateParams app Notifications]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Notifications)))

(defn ShowPictureController
  [$scope $stateParams app Pictures]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Pictures)))

(defn ShowPictureMinimalController
  [$scope $stateParams app Pictures]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Pictures)))

(defn ShowRequestTokenController
  [$scope $stateParams app RequestTokens]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app RequestTokens)))

(defn ShowServiceController
  [$scope $stateParams app Services]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Services)))

(defn ShowStreamController
  [$scope $stateParams app Streams]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Streams)))

(defn ShowStreamMinimalController
  [$scope $stateParams app Streams]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Streams))
  (set! (.-toggle $scope)
        (fn []
          (let [shown? (not (.-formShown $scope))]
            (set! (.-formShown $scope) shown?)
            (set! (.-btnLabel $scope) (if shown? "-" "+"))))))

(defn ShowSubscriptionController
  [$scope $stateParams app Subscriptions]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Subscriptions)))

(defn ShowUserController
  [$scope $stateParams app Users]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Users)))

(defn ShowUserMinimalController
  [$scope $stateParams app Users]
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app Users)))

(item-directive
 jiksnu "activity"
 #js ["$scope" "$stateParams" "app" "Activities" ShowActivityController])

(item-directive
 jiksnu "album"
 #js ["$scope" "$stateParams" "app" "Albums" ShowAlbumController])

(item-directive
 jiksnu "album-minimal"
 #js ["$scope" "$stateParams" "app" "Albums" ShowAlbumMinimalController])

(item-directive
 jiksnu "client"
 #js ["$scope" "$stateParams" "app" "Clients" ShowClientController])

(item-directive
 jiksnu "client-minimal"
 #js ["$scope" "$stateParams" "app" "Clients"  ShowClientMinimalController])

(item-directive
 jiksnu "conversation"
 #js ["$scope" "$stateParams" "app" "Conversations"  ShowConversationController])

(item-directive
 jiksnu "domain"
 #js ["$scope" "$stateParams" "app" "Domains"  ShowDomainController])

(item-directive
 jiksnu "followers-minimal"
 #js ["$scope" "$stateParams" "app" "Subscriptions"  ShowFollowersMinimalController])

(item-directive
 jiksnu "following-minimal"
 #js ["$scope" "$stateParams" "app" "Subscriptions"  ShowFollowingMinimalController])

(item-directive
 jiksnu "group"
 #js ["$scope" "$stateParams" "app" "Groups"  ShowGroupController])

(item-directive
 jiksnu "group-minimal"
 #js ["$scope" "$stateParams" "app" "Groups"  ShowGroupMinimalController])

(item-directive
 jiksnu "group-membership"
 #js ["$scope" "$stateParams" "app" "GroupMemberships"  ShowGroupMembershipController])

(item-directive
 jiksnu "group-membership-minimal"
 #js ["$scope" "$stateParams" "app" "GroupMemberships"  ShowGroupMembershipMinimalController])

(item-directive
 jiksnu "like"
 #js ["$scope" "$stateParams" "app" "Likes" ShowLikeController])

(item-directive
 jiksnu "liked-by"
 #js ["$scope" "$stateParams" "app" "Likes" ShowLikedByController])

(item-directive
 jiksnu "notification"
 #js ["$scope" "$stateParams" "app" "Notifications"  ShowNotificationController])

(item-directive
 jiksnu "picture"
 #js ["$scope" "$stateParams" "app" "Pictures"  ShowPictureController])

(item-directive
 jiksnu "picture-minimal"
 #js ["$scope" "$stateParams" "app" "Pictures" ShowPictureMinimalController])

(item-directive
 jiksnu "request-token"
 #js ["$scope" "$stateParams" "app" "RequestToken" ShowRequestTokenController])

(item-directive
 jiksnu "service"
 #js ["$scope" "$stateParams" "app" "Services" ShowServiceController])

(item-directive
 jiksnu "stream"
 #js ["$scope" "$stateParams" "app" "Streams" ShowStreamController])

(item-directive
 jiksnu "stream-minimal"
 #js ["$scope" "$stateParams" "app" "Streams" ShowStreamMinimalController])

(item-directive
 jiksnu "subscription"
 #js ["$scope" "$stateParams" "app" "Subscriptions"  ShowSubscriptionController])

(item-directive
 jiksnu "user"
 #js ["$scope" "$stateParams" "app" "Users" ShowUserController])

(item-directive
 jiksnu "user-minimal"
 #js ["$scope" "$stateParams" "app" "Users" ShowUserMinimalController])
