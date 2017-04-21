(ns jiksnu.components.state-components
  (:require [jiksnu.app :refer [jiksnu]]
            [jiksnu.helpers :as helpers]
            [jiksnu.protocols :as p]
            [taoensso.timbre :as timbre]))

(defn AvatarPageController [])

(.component
 jiksnu "avatarPage"
 #js {:controller #js [AvatarPageController]
      :templateUrl "/templates/avatar-page"})

(defn LoginPageController
  [$scope $state app $mdToast]
  (set! (.-login $scope)
        (fn []
          (let [username (.-username $scope)
                password (.-password $scope)]
            (-> (.login app username password)
                (.then (fn [r] (.go $state "home"))
                       (fn [e] (.showSimple $mdToast "login failed"))))))))

(.component
 jiksnu "loginPage"
 #js {:controller #js ["$scope" "$state" "app" "$mdToast" LoginPageController]
      :templateUrl "/templates/login-page"})

(defn AuthorizeClientController
  [$location $scope $stateParams app RequestTokens]
  (timbre/info "Location: " $location)
  (timbre/info "State Params: " $stateParams)
  (set! (.-id $scope) (aget (.search $location) "oauth_token"))
  (this-as $ctrl (helpers/init-item $ctrl $scope $stateParams app RequestTokens)))

(.component
 jiksnu "authorizeClient"
 #js {:controller #js ["$location" "$scope" "$stateParams" "app" "RequestTokens"
                       AuthorizeClientController]
      :templateUrl "/templates/authorize-client"})

(defn LogoutController [])

(.controller
 jiksnu "LogoutController"
 #js [LogoutController])

(defn RegisterPageController
  [app $scope]
  (set! (.-register $scope)
        (fn []
          (-> (p/register app $scope)
              (.then (fn [data]
                       ;; Should we copy the whole data object?
                       (set! (.. app -data -user) (.. data -data -user))
                       (-> (.fetchStatus app)
                           (.then #(p/go app "home")))))))))

(.component
 jiksnu "registerPage"
 #js {:controller #js ["app" "$scope" RegisterPageController]
      :templateUrl "/templates/register-page"})

(defn SettingsPageController [])

(.component
 jiksnu "settingsPage"
 #js {:controller #js [SettingsPageController]
      :templateUrl "/templates/settings-page"})
