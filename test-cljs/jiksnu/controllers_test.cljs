(ns jiksnu.controllers-test
  (:require [purnam.test]
            jiksnu.app
            jiksnu.controllers
            [taoensso.timbre :as timbre
             :refer-macros (log  trace  debug  info  warn  error  fatal  report
                                 logf tracef debugf infof warnf errorf fatalf reportf
                                 spy get-env log-env)])
  (:use-macros [purnam.core :only [obj arr !]]
               [purnam.test :only [describe it is]]
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

(def nav-bar-controller "NavBarController")

(js/describe nav-bar-controller
  (fn []
    (let [controller (atom nil)]
      (js/beforeEach (js/module "jiksnu"))

      (js/beforeEach
       (js/inject
        #js ["$controller" (fn [_$controller_]
                             (reset! controller _$controller_)
                             (info "controller reset"))]))

      (js/beforeEach
       (fn []
         (debug "before each test")
         (set! mock-fetch
               (fn []
                 #js
                 {:then (fn [f]
                          (debug "running replacement mock fetch")
                          #_(f))}))))


      (js/it "should call fetchStatus"
        (fn []




          (info "it")
          (let [$scope #js {:$watch (fn [] (info "Watching"))}
                c (@controller nav-bar-controller
                   #js {:$scope $scope
                        :app (mock-app)})]
            (is (+ 2 2) 4)
            (is (.-loaded $scope) false)
            (js/console.log $scope))))
      nil)))


(describe.controller
 {:doc "jiksnu.NavBarController"
  :module jiksnu
  :controller NavBarController
  :provides {app (mock-app)
             app2 #js {:foo "baz"}}}

 (js/afterEach
  (fn []
    (debug "after each test")))

 (it "should bind the app service to app2"
   (is $scope.app2.foo "bar"))

 (it "should be unloaded by default"
   (is (.-loaded $scope) false))

 (debug "outside it")

 #_(describe {:doc "has a mocked fetch"}



   (it "should call fetchStatus"
     (debug "inside it")
     (js/console.log "inside it")

    (debug "inside it - before is")
    (is (.-loaded $scope) true)
    (debug "inside it - after is"))))
