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

      (is $scope.app2.foo "bar")))

  (let [list-streams-controller "ListStreamsController"]
    (describe {:doc list-streams-controller}
      (js/beforeEach
       (fn []
         (let [user #js {:_id "foo"}]
           (set! (.-user $scope) user))))

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
      (js/beforeEach
       (fn []
         (set! (.-id $scope) "1")))

      (describe {:doc "deleteRecord"}
        (js/it "sends a delete action"
          (fn [done]
            (let [conversation #js {:id "1"}]

              (set! (.-init $scope)
                    (fn [id]
                      (timbre/info "mocked init")
                      (set! (.-item $scope) conversation)
                      (set! (.-loaded $scope) true)))

              (set! (.-deleteRecord $scope)
                    (fn [item]
                      (timbre/info "deleting record")
                      (js/console.log "Conversation:" item)
                      (let [d (.defer $q)]
                        (.reject d true)
                        (.-promise d))
                      ;; (.when $q "foo")
                      )
                    )


              (let [response (.deleteRecord $scope conversation)]
                (js/console.log "app" app)
                (js/console.log "response" response)
                (-> response
                    (.then
                        (fn [r]
                          (js/console.info "r" r)
                          (.toBeDefined (js/expect r))
                          r)
                        (fn [r]
                            (js/console.warn "r" r)

                          r
                            ))
                    (.finally (fn []
                                (timbre/info "Done")
                                (js/done)
                                )))

                (@$controller show-conversation-controller injections)
                (.$apply $scope)

                )))))
      ))
  )
