(ns jiksnu.controllers-test
  (:require [purnam.test]
            jiksnu.app
            jiksnu.controllers
            [taoensso.timbre :as timbre
             :refer-macros (log  trace  debug  info  warn  error  fatal  report
                                 logf tracef debugf infof warnf errorf fatalf reportf
                                 spy get-env log-env)])
  (:use-macros [purnam.core :only [obj arr !]]
               [purnam.test :only [describe it is fact]]
               [gyr.test    :only [describe.ng describe.controller
                                   it-uses it-compiles]]))

(timbre/set-level! :debug)

(def app-atom (atom nil))

(defn mock-fetch
  []
  #js
  {:then (fn [f]
           (debug "running original mock fetch")
           #_(f))})

(defn mock-app
  []
  (debug "Getting mocked app")
  #js {:foo "bar"
       :logout (fn [])
       :fetchStatus mock-fetch})

(def jiksnu "jiksnu")
(def nav-bar-controller "NavBarController")

(declare $q)
(declare $rootScope)
(declare $scope)
(declare c)
(def $controller (atom nil))

(describe "jiksnu"
  (js/beforeEach (js/module "jiksnu"))

  (js/beforeEach
   (js/inject
    #js ["$controller" "$rootScope" "$q"
         (fn [_$controller_ _$rootScope_ _$q_]
           (reset! $controller _$controller_)
           (set! $rootScope _$rootScope_)
           (set! $q _$q_))]))

  (describe nav-bar-controller
    (js/beforeEach
     (fn []
       (set! mock-fetch
             (fn []
               #js
               {:then (fn [f] #_(f))}))
       (set! $scope (.$new $rootScope))))

    (it "should be unloaded by default"
      (@$controller nav-bar-controller #js {:$scope $scope :app (mock-app)})
      (is $scope.loaded false))

    (it "should call fetchStatus"
      (set! mock-fetch
            (fn []
              #js
              {:then (fn [f]
                       (info "replacement")
                       (f))}))

      (@$controller nav-bar-controller #js {:$scope $scope :app (mock-app)})
      (is $scope.loaded true))

    (it "should bind the app service to app2"
      (@$controller "NavBarController" #js {:$scope $scope :app (mock-app)})

      (is $scope.app2.foo "bar"))

    (fact [[{:doc "a fact"}]]
          (+ 1 1) => 2
          (+ 2 2) => 4)

    nil))
