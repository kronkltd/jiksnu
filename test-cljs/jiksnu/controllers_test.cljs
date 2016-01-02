(ns jiksnu.controllers-test
  (:require [purnam.test :refer-macros [describe it is fact]]
            jiksnu.app
            jiksnu.controllers
            [taoensso.timbre :as timbre])
  (:use-macros [gyr.test :only [describe.ng describe.controller it-uses it-compiles]]))

(timbre/set-level! :debug)

(def app-atom (atom nil))

(def jiksnu "jiksnu")
(def nav-bar-controller "NavBarController")

(declare $q)
(declare $rootScope)
(declare $scope)
(declare c)
(declare app)
(declare injections)
(def $controller (atom nil))

(describe {:doc "jiksnu"}
  (js/beforeEach (js/module "jiksnu"))

  (js/beforeEach
   (js/inject
    #js ["$controller" "$rootScope" "$q"
         "app"
         (fn [_$controller_ _$rootScope_ _$q_ _app_]
           (reset! $controller _$controller_)
           (set! app _app_)
           (set! $rootScope _$rootScope_)
           (set! $scope (.$new $rootScope))
           (set! $q _$q_)
           (set! injections #js {:$scope $scope :app app}))]))

  (describe {:doc nav-bar-controller}
    (js/beforeEach
     (fn []
       (let [mock-then (fn [f] #_(f))
             mock-response #js {:then mock-then}]
         (set! (.-fetchStatus app) (constantly mock-response)))))

    (it "should be unloaded by default"
      (@$controller nav-bar-controller injections)
      (is $scope.loaded false))

    (it "should call fetchStatus"
      (let [mock-then (fn [f]
                        (timbre/info "replacement")
                        (f))
            mock-response #js {:then mock-then}]
        (set! (.-fetchStatus app) (constantly mock-response))
        (@$controller nav-bar-controller injections)
        (is $scope.loaded true)))

    (it "should bind the app service to app2"
      (set! (.-foo app) "bar")
      (@$controller "NavBarController" injections)

      (is $scope.app2.foo "bar"))

    (fact [[{:doc "a fact"}]]
          (+ 1 1) => 2
          (+ 2 2) => 4))

  (describe {:doc "ListStreamsController"}
    (js/beforeEach
     (fn []
       (let [user #js {:_id "foo"}]
         (set! (.-user $scope) user))))

    (describe {:doc "addStream"}
      (it "sends an add-stream notice to the server"
        (let [stream-name "bar"
              params #js {:name stream-name}]
          (@$controller "ListStreamsController" injections)
          (set! (.-stream $scope) params)

          (-> (.addStream $scope)
              (.then (fn [response]
                       (is response nil)))))))

    (describe {:doc "delete"})))
