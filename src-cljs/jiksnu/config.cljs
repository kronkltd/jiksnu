(ns jiksnu.config
  (:require jiksnu.app
            [jiksnu.helpers :as helpers]
            jiksnu.providers
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders.core])
  (:use-macros [gyr.core :only [def.config]]))

(def.config jiksnu [$stateProvider $urlRouterProvider $locationProvider
                    appProvider DSProvider DSHttpAdapterProvider
                    hljsServiceProvider NotificationProvider
                    uiSelectConfig]


  ;; (timbre/merge-config!
  ;;  {:appenders {:console (appenders.core/console-appender {:raw-output? true})}}
  ;;  )

  (.setOptions hljsServiceProvider #js {:tabReplace "  "})

  (set! (.-theme uiSelectConfig) "bootstrap")

  (js/angular.extend (.-defaults DSProvider)
                     #js {:idAttribute "_id"
                          :basePath    "/model"})

  (js/angular.extend (.-defaults DSHttpAdapterProvider)
                     #js {:log false})

  (.setOptions NotificationProvider #js {:startTop 20})

  (.otherwise $urlRouterProvider "/")
  (-> $locationProvider
      (.hashPrefix "!")
      (.html5Mode true))
  (helpers/add-states $stateProvider helpers/states))
