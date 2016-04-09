(ns jiksnu.providers-test
  (:require jiksnu.providers
            [purnam.test :refer-macros [beforeEach describe is it]]
            [taoensso.timbre :as timbre]))

(declare app)
(declare $httpBackend)
(declare $q)
(declare $rootScope)

(describe {:doc "jiksnu.providers"}
  (js/beforeEach (js/module "jiksnu"))

  (js/beforeEach
   (js/inject
    #js ["app" "$httpBackend" "$q" "$rootScope"
         (fn [_app_ _$httpBackend_ _$q_ _$rootScope_]
           (set! app _app_)
           (set! $httpBackend _$httpBackend_)
           (set! $q _$q_)
           (set! $rootScope _$rootScope_)
           (.. $httpBackend (whenGET #"/templates/.*") (respond "<div></div>"))
           (.. $httpBackend (whenGET #"/model/.*")     (respond "{}")))]))

  (js/afterEach (fn [] (.verifyNoOutstandingRequest $httpBackend)))

  (describe {:doc "app"}
    (describe {:doc ".addStream"}
      (it "should add the stream"
        (let [stream-name "foo"
              response (atom nil)]

          ;; route: streams-api/collection :post
          (.. $httpBackend (expectPOST "/model/streams") (respond (constantly #js [200 stream-name])))

          (.. app
              (addStream stream-name)
              (then #(reset! response %)))

          (.flush $httpBackend)
          (is @response stream-name))))

    (describe {:doc ".connect"}
      (it "should open a websocket connection"
        (let [spy (.. (js/spyOn app "send")
                      -and
                      (returnValue "foo"))])
        (timbre/spy (.connect app))))

    (describe {:doc ".follow"}
      (describe {:doc "when not authenticated"}
        (js/it "should be rejected"
          (fn [done]
            (.. app
                (follow)
                (then #(.. (js/expect true) (toBeFalsy))
                      #(.. (js/expect true) (toBeTruthy)))
                (finally done))
            (.$digest $rootScope)))))

    (describe {:doc ".getUser"}
      (describe {:doc "when not authenticated"}
        (js/it "resolves to nil"
          (fn [done]
            (.. app
                (getUser)
                (then #(.. (js/expect %)    (toBeNull))
                      #(.. (js/expect true) (toBeFalsy)))
                (finally done))
            (.flush $httpBackend)
            (.$digest $rootScope))))
      (describe {:doc "when authenticated"}
        (js/it "returns that user"
          (fn [done]
            (let [Users (.inject app "Users")
                  id "acct:foo@example.com"
                  user #js {:_id id}]
              (.. (js/spyOn app   "getUserId") -and (returnValue id))
              (.. (js/spyOn Users "find")      -and (returnValue ($q #(% user))))
              (.. app
                  (getUser)
                  (then #(.. (js/expect %)    (toBe user))
                        #(.. (js/expect true) (toBeFalsy)))
                  (finally done))
              (.$digest $rootScope)
              (.. (js/expect (.-getUser app))   (toHaveBeenCalled))
              (.. (js/expect (.-getUserId app)) (toHaveBeenCalled)))))))

    (describe {:doc ".invokeAction"}
      (js/it "sends a message"
        (fn [done]
          (let [model-name "user"
                action-name "delete"
                id "acct:user@example.com"]
            (.. (js/spyOn app "send") -and (returnValue ($q #(%))) )
            (.. app
                (invokeAction model-name action-name id)
                (then #(.. (js/expect true)  (toBeTruthy))
                      #(.. (js/expect false) (toBeTruthy)))
                (finally done)))
          (.$digest $rootScope))))))
