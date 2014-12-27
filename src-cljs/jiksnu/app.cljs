(ns jiksnu.app
  (:require [jiksnu.helpers :as helpers])
  (:use-macros [gyr.core :only [def.module def.config]]))

(def.module jiksnu [ui.router ui.bootstrap angularMoment
                    ui.bootstrap.tabs])

(def.config jiksnu [$stateProvider $urlRouterProvider $locationProvider]
  (.otherwise $urlRouterProvider "/")
  (-> $locationProvider
      (.hashPrefix "!")
      (.html5Mode true))
  (helpers/add-states $stateProvider helpers/states))
