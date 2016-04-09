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
          (.. $httpBackend (expectPOST "/model/streams") (respond (fn [] #js [200 stream-name])))

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

    (describe {:doc ".getUser"}
      (describe {:doc "when not authenticated"}
        (js/it "resolves to nil"
          (fn [done]
            (.. app
                (getUser)
                (then (fn [user] (is user true))
                      (fn [user] (is user nil)))
                (finally done))
            (.flush $httpBackend)
            (.$apply $rootScope))))
      (describe {:doc "when authenticated"}
        (js/it "returns that user"
          (fn [done]
            (let [id "acct:foo@example.com"
                  user #js {:_id id}]
              (.. (js/spyOn app "getUserId") -and (returnValue id))
              (.. (js/spyOn app "getUser")   -and (returnValue ($q (fn [resolve] (resolve user)))))
              (.. app
                  (getUser)
                  (then (fn [r]
                          (is r user)
                          (timbre/infof "r: %s" r)))
                  (finally done))
              (.$apply $rootScope)
              (.. (js/expect (.-getUser app)) (toHaveBeenCalled)))))))))
