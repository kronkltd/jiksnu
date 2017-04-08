(ns jiksnu.config
  (:require [jiksnu.app :refer [jiksnu]]
            [jiksnu.helpers :as helpers]
            jiksnu.providers
            [jiksnu.registry :as registry]))

(defn jiksnu-config
  [$stateProvider $urlRouterProvider $locationProvider appProvider DSProvider
   DSHttpAdapterProvider hljsServiceProvider $mdThemingProvider]

  (.setOptions hljsServiceProvider #js {:tabReplace "  "})

  (-> $mdThemingProvider
      (.theme "default")
      (.primaryPalette registry/pallete-color))

  (js/angular.extend (.-defaults DSProvider)
                     #js {:idAttribute "_id"
                          :basePath    "/model"})

  (js/angular.extend (.-defaults DSHttpAdapterProvider)
                     #js {:log false})

  (.otherwise $urlRouterProvider "/")
  (-> $locationProvider
      (.hashPrefix "!")
      (.html5Mode true))
  (helpers/add-states $stateProvider registry/route-data))

(.config
 jiksnu
 #js ["$stateProvider" "$urlRouterProvider" "$locationProvider"
      "appProvider" "DSProvider" "DSHttpAdapterProvider"
      "hljsServiceProvider" "$mdThemingProvider" jiksnu-config])
