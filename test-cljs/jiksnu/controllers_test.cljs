(ns jiksnu.controllers-test
  (:require [purnam.test]
            jiksnu.app
            jiksnu.controllers)
  (:use-macros [purnam.core :only [obj arr !]]
               [purnam.test :only [describe it is]]
               [gyr.test    :only [describe.ng describe.controller
                                   it-uses it-compiles]]))

(def app-atom (atom nil))

(defn mock-fetch
  []
  #js
  {:then (fn [f]
           (js/console.log "running original mock fetch")
           #_(f))})

(defn mock-app
  []
  (js/console.log "Getting mocked app")
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
    (js/console.log "after each test")))

 (it "should bind the app service to app2"
   (is $scope.app2.foo "bar"))

 (it "should be unloaded by default"
   (is (.-loaded $scope) false))

 (js/console.log "outside it")

 (describe {:doc "has a mocked fetch"}
   (js/beforeEach
    (fn []
      (.log js/console "before each test")
      (set! mock-fetch
            (fn []
              #js
              {:then (fn [f]
                       (js/console.log "running replacement mock fetch")
                       (f))}))))


   (it "should call fetchStatus"
    (js/console.log "inside it")

    (js/console.log "inside it - before is")
    (is (.-loaded $scope) true)
    (js/console.log "inside it - after is"))))
