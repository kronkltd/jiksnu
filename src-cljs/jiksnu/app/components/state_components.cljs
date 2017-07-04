(ns jiksnu.app.components.state-components
  (:require [jiksnu.app :refer [jiksnu]]
            [jiksnu.app.helpers :as helpers]
            [jiksnu.app.protocols :as proto]
            [taoensso.timbre :as timbre]))

(defn AvatarPageController [])

(.component
 jiksnu "avatarPage"
 #js {:controller #js [AvatarPageController]
      :templateUrl "/templates/avatar-page"})

(defn LoginPageController
  [$scope $state app $mdToast]
  (set! $scope.login
        (fn []
          (let [username $scope.username
                password $scope.password]
            (-> (proto/login app username password)
                (.then (fn [_] (.go $state "home"))
                       (fn [_] (.showSimple $mdToast "login failed"))))))))

(.component
 jiksnu "loginPage"
 #js {:controller #js ["$scope" "$state" "app" "$mdToast" LoginPageController]
      :templateUrl "/templates/login-page"})

(defn AuthorizeClientController
  [$location $scope app RequestTokens]
  (timbre/info "Location: " $location)
  (set! $scope.id (aget (.search $location) "oauth_token"))
  (this-as $ctrl (helpers/init-item $ctrl $scope app RequestTokens)))

(.component
 jiksnu "authorizeClient"
 #js {:controller #js ["$location" "$scope" "app" "RequestTokens" AuthorizeClientController]
      :templateUrl "/templates/authorize-client"})

(defn LogoutController [])

(.controller
 jiksnu "LogoutController"
 #js [LogoutController])

(defn RegisterPageController
  [app $scope]
  (set! $scope.register
        (fn []
          (-> (proto/register app $scope)
              (.then (fn [data]
                        ;; Should we copy the whole data object?
                        (set! app.data.user (.. data -data -user))
                        (-> (proto/fetch-status app)
                            (.then #(proto/go app "home")))))))))

(.component
 jiksnu "registerPage"
 #js {:controller #js ["app" "$scope" RegisterPageController]
      :templateUrl "/templates/register-page"})

(defn SettingsPageController [])

(.component
 jiksnu "settingsPage"
 #js {:controller #js [SettingsPageController]
      :templateUrl "/templates/settings-page"})
