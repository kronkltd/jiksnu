(ns jiksnu.config
  (:require jiksnu.app
            [jiksnu.helpers :as helpers]
            jiksnu.providers)
  (:use-macros [gyr.core :only [def.config]]))

(def.config jiksnu [$stateProvider $urlRouterProvider $locationProvider
                    appProvider DSProvider]

  ;; (js/console.log (.-defaults DSProvider))
  (js/angular.extend (.-defaults DSProvider) (js-obj
                                              "idAttribute" "_id"
                                              "basePath" "/model"))

  (.otherwise $urlRouterProvider "/")
  (-> $locationProvider
      (.hashPrefix "!")
      (.html5Mode true))
  (helpers/add-states $stateProvider helpers/states))
