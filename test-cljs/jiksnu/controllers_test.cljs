(ns jiksnu.controllers-test
  (:require [purnam.test]
            jiksnu.app
            jiksnu.controllers)
  (:use-macros [purnam.core :only [obj arr !]]
               [purnam.test :only [describe it is]]
               [gyr.test    :only [describe.ng describe.controller
                                   it-uses it-compiles]]))

(def log (.-log js/console))

(describe.controller
 {:doc "NavBarController"
  :module jiksnu
  :controller NavBarController
  :provides {app #js {:foo "bar"}}
  ;; :inject [[app
  ;;           ([app]
  ;;            ;; (js/console.log "app" app)

  ;;            )
  ;;           ]]
  }


 (js/beforeEach (fn []
                  (js/console.log "I'm before")

                  ;; (js/module
                  ;;  (fn [$provide]
                  ;;    (js/console.log $provide)
                  ;;    ))
                  ))

 (it "should bind the app service to app2"
   ;; (js/console.log "app:" app)
   ;; (js/console.log "$scope-app2:" (.-app2 $scope))
   (is $scope.app2.foo "bar")
   (is 1 1))

 #_(it "should be unloaded by default"
   ;; (js/console.log "$scope" (.-app $scope))
   (is (.-loaded $scope) false)))
