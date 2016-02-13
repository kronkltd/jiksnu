(ns jiksnu.controllers-test
  (:require jiksnu.app
            jiksnu.controllers
            [taoensso.timbre :as timbre])
  (:use-macros [gyr.test :only [describe.ng describe.controller it-uses it-compiles]]
               [purnam.test :only [describe it is fact beforeEach]]))

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

(describe {:doc "jiksnu.controllers"}
  (beforeEach (js/module "jiksnu"))

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
           (-> (.when $httpBackend "GET" #"/templates/.*")
               (.respond "<div></div>"))

           (set! injections #js {:$scope $scope :app app}))]))

  (describe {:doc "FollowButtonController"}
    (let [item-id "acct:foo@example.com"
          controller-name "FollowButtonController"]
      (describe {:doc "isActor"}
        (describe {:doc "When not authenticated"}
          (it "should return false"
            (@$controller controller-name injections)
            (set! (.-item $scope) #js {:_id item-id})
            (is (.isActor $scope) false)))
        (describe {:doc "When authenticated"}
          (describe {:doc "As another user"}
            (it "should return false"
              (@$controller controller-name injections)
              (set! (.-domain (.-data app)) "example.com")
              (set! (.-user (.-data app)) "bar")
              (set! (.-item $scope) #js {:_id item-id})
              (is (.isActor $scope) false)))
          (describe {:doc "As the actor"}
            (it "should return true"
              (@$controller controller-name injections)
              (set! (.-domain (.-data app)) "example.com")
              (set! (.-user (.-data app)) "foo")
              (set! (.-item $scope) #js {:_id item-id})
              (is (.isActor $scope) true)))))))

  (let [nav-bar-controller "NavBarController"]
    (describe {:doc nav-bar-controller}
      (beforeEach
       (let [mock-then (fn [f] #_(f))
             mock-response #js {:then mock-then}]
         (set! (.-fetchStatus app) (constantly mock-response))))

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

        (is $scope.app2.foo "bar"))))

  (let [new-group-controller "NewGroupController"]
    (describe {:doc new-group-controller}
      (beforeEach
       (timbre/info "Set up controller")
       )

      (describe {:doc ".submit"}
        (it "Should send the form"
          (@$controller new-group-controller injections)
          (.submit $scope)

          )
        )
      )
    )

  (let [list-streams-controller "ListStreamsController"]
    (describe {:doc list-streams-controller}
      (beforeEach
       (let [user #js {:_id "foo"}]
         (set! (.-user $scope) user)))

      (describe {:doc "addStream"}
        (it "sends an add-stream notice to the server"
          (let [stream-name "bar"
                params #js {:name stream-name}]
            (@$controller list-streams-controller injections)
            (set! (.-stream $scope) params)

            (-> (.addStream $scope)
                (.then (fn [response]
                         (is response nil)))))))

      (describe {:doc "delete"})))

  (let [show-conversation-controller "ShowConversationController"]
    (describe {:doc show-conversation-controller}
      (beforeEach
       (set! (.-id $scope) "1")
       (set! (.-init $scope)
             (fn [id]
               (timbre/info "mocked init")
               (set! (.-item $scope) #js {:id "1"})
               (set! (.-loaded $scope) true))))

      (describe {:doc "deleteRecord"}
        (js/it "sends a delete action"
          (fn [done]
            (@$controller show-conversation-controller injections)

            (let [spy (.. (js/spyOn app "invokeAction")
                          -and
                          (returnValue
                           (.when $q #js {})))
                  response (.deleteRecord $scope (.-item $scope))]
              (js/console.log response)
              (-> response
                  (.then
                   (fn [r]
                     (timbre/info "then")
                     (.toBeDefined (js/expect r))
                     r)
                   (fn [r]
                     (timbre/info "error")
                     (.toBeDefined (js/expect r))))
                  (.finally (fn []
                              (timbre/info "Done")
                              (js/done))))
              (.$apply $scope)

              (.toHaveBeenCalled (js/expect spy))))))
      ))
  )
