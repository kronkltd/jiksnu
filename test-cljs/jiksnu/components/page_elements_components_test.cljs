(ns jiksnu.components.page-element-components-test
  (:require jiksnu.main
            jiksnu.components.page-element-components
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

(js/describe "jiksnu.components.page-element-components"
  (fn []
    (js/beforeEach
     (fn []
       (js/module "jiksnu")))

    (js/beforeEach (fn [] (js/installPromiseMatchers)))

    (js/beforeEach
     (fn []
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
               (set! injections #js {:$scope $scope :app app}))])))

    (let [controller-name "FollowButtonController"]
      (js/describe controller-name
        (fn []
          (let [item-id "acct:foo@example.com"]
            (js/describe ".isActor"
              (fn []
                (js/describe "When not authenticated"
                  (fn []
                    (js/it "should return false"
                      (fn []
                        (@$controller controller-name injections)
                        (set! (.-item $scope) #js {:_id item-id})
                        (.. (js/expect (.isActor $scope)) toBeFalsy)))))
                (js/describe "When authenticated"
                  (fn []
                    (js/describe "As another user"
                      (fn []
                        (js/it "should return false"
                          (fn []
                            (@$controller controller-name injections)
                            (set! (.-domain (.-data app)) "example.com")
                            (set! (.-user (.-data app)) "bar")
                            (set! (.-item $scope) #js {:_id item-id})
                            (.. (js/expect (.isActor $scope)) toBeFalsy)))))
                    (js/describe "As the actor"
                      (fn []
                        (js/it "should return true"
                          (fn []
                            (@$controller controller-name injections)
                            (set! (.-domain (.-data app)) "example.com")
                            (set! (.-user (.-data app)) "foo")
                            (set! (.-item $scope) #js {:_id item-id})
                            (.. (js/expect (.isActor $scope)) toBeTruthy)))))))))
            (js/describe ".isFollowing"
              (fn []
                (js/describe "when not authenticated"
                  (fn []
                    (js/it "should resolve to falsey"
                      (fn []
                        (@$controller controller-name injections)
                        (set! (.-item $scope) #js {:_id item-id})
                        (let [p (.isFollowing $scope)]
                          (.$apply $scope)
                          (.. (js/expect p) (toBeResolvedWith nil)))))))))))))

    (let [controller-name "NavBarController"]
      (js/describe controller-name
        (fn []
          (js/beforeEach
           (fn []
             (let [mock-then (fn [f] #_(f))
                   mock-response #js {:then mock-then}]
               (set! (.-fetchStatus app) (constantly mock-response)))))

          (js/it "should be unloaded by default"
            (fn []
              (@$controller controller-name injections)
              (.. (js/expect (.-loaded $scope)) toBeFalsy)))

          (js/it "should call fetchStatus"
            (fn []
              (let [mock-then (fn [f] (f))
                    mock-response #js {:then mock-then}]
                (set! (.-fetchStatus app) (constantly mock-response))
                (@$controller controller-name injections)
                (.. (js/expect (.-loaded $scope)) toBeTruthy))))

          (js/it "should bind the app service to app2"
            (fn []
              (set! (.-foo app) "bar")
              (@$controller controller-name injections)
              (.. (js/expect (some-> $scope .-app2 .-foo)) (toBe "bar")))))))))
