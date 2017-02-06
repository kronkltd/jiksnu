(ns jiksnu.components.index-components
  (:require [inflections.core :as inf]
            [jiksnu.app :refer [jiksnu]]
            [jiksnu.helpers :as helpers]
            [taoensso.timbre :as timbre]))

(defn page-controller
  [module page-name]
  (.controller
   module (str "Index" (inf/camel-case page-name) "Controller")
   #js ["$scope" "$rootScope" "app" "pageService" "subpageService"
        (fn [$scope $rootScope app pageService subpageService]
          (helpers/init-page $scope $rootScope app page-name)
          (set! (.-app $scope) app)
          (set! (.-refresh $scope) (fn [] (.init $scope)))
          (.init $scope))])
  (.component
   module (str "index" (inf/camel-case page-name))
   #js {:templateUrl (str "/templates/index-" (inf/dasherize page-name))
        :controller (str "Index" (inf/camel-case page-name) "Controller")
        :bindings #js {:id "@" :item "="}}))

;; TODO: Auto register for each defined page
(page-controller jiksnu "activities")
(page-controller jiksnu "albums")
(page-controller jiksnu "clients")
(page-controller jiksnu "conversations")
(page-controller jiksnu "domains")
(page-controller jiksnu "feed-sources")
(page-controller jiksnu "groups")
(page-controller jiksnu "group-memberships")
(page-controller jiksnu "likes")
(page-controller jiksnu "notifications")
(page-controller jiksnu "pictures")
(page-controller jiksnu "request-tokens")
(page-controller jiksnu "resources")
(page-controller jiksnu "services")
(page-controller jiksnu "streams")
(page-controller jiksnu "subscriptions")
(page-controller jiksnu "users")
