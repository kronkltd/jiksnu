(ns jiksnu.config
  (:require jiksnu.app
            [jiksnu.helpers :as helpers]
            jiksnu.providers)
  (:use-macros [gyr.core :only [def.config]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n]]))

(def.config jiksnu [$stateProvider $urlRouterProvider $locationProvider
                    appProvider wsProvider
                    DSProvider]

  (.setUrl wsProvider (str "ws"
                           (when (= (? js/window.location.protocol) "https:")
                             "s")
                           "://"
                           (? js/window.location.host)
                           "/"))


  ;; (js/console.log (.-defaults DSProvider))
  (.extend js/angular (.-defaults DSProvider) (obj
                                               :idAttribute "_id"
                                               :basePath "/model"
                                               ))

  ;; (! DSProvider.defaults.idAttribute "_id")
  ;; (! DSProvider.defaults.basePath "/model")

  (.otherwise $urlRouterProvider "/")
  (-> $locationProvider
      (.hashPrefix "!")
      (.html5Mode true))
  (helpers/add-states $stateProvider helpers/states))
