(ns jiksnu.config
  (:require jiksnu.app
            [jiksnu.helpers :as helpers]
            jiksnu.providers)
  (:use-macros [gyr.core :only [def.config]]))

(def.config jiksnu [$stateProvider $urlRouterProvider $locationProvider
                    appProvider wsProvider
                    DSProvider]


  (if-let [location js/window.location]
    (let [scheme (str "ws" (when (= (.-protocol location) "https:") "s"))
          host (.-host location)]
      (.setUrl wsProvider (str scheme "://" host "/")))
    (throw (js/Exception. "No location available")))

  ;; (js/console.log (.-defaults DSProvider))
  (js/angular.extend (.-defaults DSProvider) (js-obj
                                              "idAttribute" "_id"
                                              "basePath" "/model"))

  (.otherwise $urlRouterProvider "/")
  (-> $locationProvider
      (.hashPrefix "!")
      (.html5Mode true))
  (helpers/add-states $stateProvider helpers/states))
