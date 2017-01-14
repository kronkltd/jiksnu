(ns jiksnu.components.page-element-components-test
  (:require jiksnu.main
            jiksnu.components.page-element-components
            [purnam.test :refer-macros [describe it is beforeEach]]
            [taoensso.timbre :as timbre]))

(timbre/set-level! :debug)

(def app-atom (atom nil))

(def jiksnu "jiksnu")

(declare $q)
(declare $rootScope)
(declare $scope)
(declare app)
(declare injections)
(declare $httpBackend)
(def $controller (atom nil))

(describe {:doc "jiksnu.components.page-element-components"}
  (beforeEach (js/module "jiksnu"))

  (js/beforeEach (fn [] (js/installPromiseMatchers)))

  (beforeEach
   (js/inject
    #js ["$controller" "$rootScope" "$q"
         "app" "$httpBackend"
         (fn [_$controller_ _$rootScope_ _$q_ _app_ _$httpBackend_]
           (reset! $controller _$controller_)
           (set! app _app_)
           (set! $rootScope _$rootScope_)
           (set! $scope (.$new $rootScope))
           (set! $q _$q_)
           (set! $httpBackend _$httpBackend_)
           (doto $httpBackend
             (.. (whenGET #"/templates/.*") (respond "<div></div>"))
             (.. (whenGET #"/model/.*")     (respond "{}")))
           (set! injections #js {:$scope $scope :app app}))]))

  (let [controller-name "FollowButtonController"]
    (describe {:doc controller-name}
      (let [item-id "acct:foo@example.com"]
        (describe {:doc ".isActor"}
          (describe {:doc "When not authenticated"}
            (it "should return false"
              (@$controller controller-name injections)
              (set! (.-item $scope) #js {:_id item-id})
              (.. (js/expect (.isActor $scope)) (toBe false))))
          (describe {:doc "When authenticated"}
            (describe {:doc "As another user"}
              (it "should return false"
                (@$controller controller-name injections)
                (set! (.-domain (.-data app)) "example.com")
                (set! (.-user (.-data app)) "bar")
                (set! (.-item $scope) #js {:_id item-id})
                (.. (js/expect (.isActor $scope)) (toBe false))))
            (describe {:doc "As the actor"}
              (it "should return true"
                (@$controller controller-name injections)
                (set! (.-domain (.-data app)) "example.com")
                (set! (.-user (.-data app)) "foo")
                (set! (.-item $scope) #js {:_id item-id})
                (.. (js/expect (.isActor $scope)) (toBe true))))))
        (describe {:doc ".isFollowing"}
          (describe {:doc "when not authenticated"}
            (it "should resolve to falsey"
              (@$controller controller-name injections)
              (set! (.-item $scope) #js {:_id item-id})
              (let [p (.isFollowing $scope)]
                (.$apply $scope)
                (.. (js/expect p) (toBeResolvedWith nil)))))))))

  (let [controller-name "NavBarController"]
    (describe {:doc controller-name}
      (beforeEach
       (let [mock-then (fn [f] #_(f))
             mock-response #js {:then mock-then}]
         (set! (.-fetchStatus app) (constantly mock-response))))

      (it "should be unloaded by default"
        (@$controller controller-name injections)
        (is $scope.loaded false))

      (it "should call fetchStatus"
        (let [mock-then (fn [f] (f))
              mock-response #js {:then mock-then}]
          (set! (.-fetchStatus app) (constantly mock-response))
          (@$controller controller-name injections)
          (.. (js/expect (.-loaded $scope)) (toBe true))))

      (it "should bind the app service to app2"
        (set! (.-foo app) "bar")
        (@$controller controller-name injections)
        (is $scope.app2.foo "bar")))))
