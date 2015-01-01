(ns jiksnu.config
  (:require jiksnu.app
            [jiksnu.helpers :as helpers]
            jiksnu.providers)
  (:use-macros [gyr.core :only [def.config]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n]]))

(def.config jiksnu [$stateProvider $urlRouterProvider $locationProvider
                    appProvider wsProvider
                    ]

  (.setUrl wsProvider "wss://renfer.name/")
  (.log js/console "Foo?: " (.-foo appProvider))

  (.otherwise $urlRouterProvider "/not-found")
  (-> $locationProvider
      (.hashPrefix "!")
      (.html5Mode true))
  (helpers/add-states $stateProvider helpers/states)

  (doto $stateProvider
    (.state "logout"
            (obj
             :controller "LogoutController"
             :url "/main/logout"))))
