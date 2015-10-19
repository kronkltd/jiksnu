(ns jiksnu.controllers-test
  (:require [purnam.test]
            jiksnu.app
            jiksnu.controllers)
  (:use-macros [purnam.core :only [obj arr !]]
               [purnam.test :only [describe it is]]
               [gyr.test    :only [describe.ng describe.controller
                                   it-uses it-compiles]]))

(defn mock-fetch
  []
  #js
  {:then (fn [f]
           (js/console.log "status then")
           #_(f))})

(def mock-app
  #js {:foo "bar"
       :logout (fn [])
       :fetchStatus mock-fetch}

  )

(describe.controller
 {:doc "NavBarController"
  :module jiksnu
  :controller NavBarController
  :provides {app mock-app
             app2 #js {:foo "baz"}}}

 (js/beforeEach
  (fn []
    (.log js/console "I'm before")
    (set! mock-fetch
          (fn []
            #js
            {:then (fn [f]
                     (js/console.log "status then 2")
                     (f))}))))

 (it "should bind the app service to app2"
   (js/console.log "inside it")
   (is $scope.app2.foo "bar"))

 (it "should be unloaded by default"
   ;; (js/console.log "$scope" (.-app $scope))
   (is (.-loaded $scope) false))

 #_(it "should call fetchStatus")

 )
