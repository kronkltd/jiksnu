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

 (describe {:doc "has a mocked fetch"}
   (js/beforeEach
    (fn []
      (debug "before each test")
      (set! mock-fetch
            (fn []
              #js
              {:then (fn [f]
                       (debug "running replacement mock fetch")
                       (f))}))))


   (it "should call fetchStatus"
     (debug "inside it")
     (js/console.log "inside it")

    (debug "inside it - before is")
    (is (.-loaded $scope) true)
    (debug "inside it - after is"))))
