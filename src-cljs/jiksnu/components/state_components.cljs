(ns jiksnu.components.state-components
  (:require jiksnu.app
            [jiksnu.helpers :as helpers]
            [taoensso.timbre :as timbre])
  (:use-macros [gyr.core :only [def.controller]]))

(def.controller jiksnu.AvatarPageController [])

(def.controller jiksnu.LoginPageController
  [$scope $state app $mdToast]
  (set! (.-login $scope)
        (fn []
          (let [username (.-username $scope)
                password (.-password $scope)]
            (-> (.login app username password)
                (.then (fn [r] (.go $state "home"))
                       (fn [e] (.showSimple $mdToast "login failed"))))))))

(def.controller jiksnu.AuthorizeClientController
  [$location $scope $stateParams app RequestTokens]
  (timbre/info "Location: " $location)
  (timbre/info "State Params: " $stateParams)
  (set! (.-id $scope) (aget (.search $location) "oauth_token"))
  (helpers/init-item $scope $stateParams app RequestTokens))

(def.controller jiksnu.LogoutController [])

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
