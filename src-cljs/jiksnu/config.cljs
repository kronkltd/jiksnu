(ns jiksnu.config
  (:require jiksnu.app
            [jiksnu.helpers :as helpers]
            jiksnu.providers
            [jiksnu.registry :as registry])
  (:use-macros [gyr.core :only [def.config]]))

(def.config jiksnu [$stateProvider $urlRouterProvider $locationProvider
                    appProvider DSProvider DSHttpAdapterProvider
                    hljsServiceProvider $mdThemingProvider]

  (.setOptions hljsServiceProvider #js {:tabReplace "  "})

  (-> $mdThemingProvider
      (.theme "default")
      (.primaryPalette "green"))

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
