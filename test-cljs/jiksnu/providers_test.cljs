(ns jiksnu.providers-test
  (:require jiksnu.main
            [purnam.test :refer-macros [beforeEach describe is it]]
            [taoensso.timbre :as timbre]))

(declare app)
(declare $httpBackend)
(declare $q)
(declare $rootScope)

(defn valid-login-response
  [method url data headers params]
  #js [204 nil #js {} "No-Content"])

(defn invalid-login-response
  [method url data headers params]
  #js [409 nil #js {} "Unauthenticated"])

(describe {:doc "jiksnu.providers"}
  (js/beforeEach (js/module "jiksnu"))

  (js/beforeEach (fn [] (js/installPromiseMatchers)))

  (js/beforeEach
   (js/inject
    #js ["app" "$httpBackend" "$q" "$rootScope"
         (fn [_app_ _$httpBackend_ _$q_ _$rootScope_]
           (set! app _app_)
           (set! $httpBackend _$httpBackend_)
           (set! $q _$q_)
           (set! $rootScope _$rootScope_)
           (doto $httpBackend
             (.. (whenGET #"/templates/.*") (respond "<div></div>"))
             (.. (whenGET #"/model/.*")     (respond "{}"))))]))

  (js/afterEach (fn [] (.verifyNoOutstandingRequest $httpBackend)))

  (describe {:doc "app"}
    (describe {:doc ".addStream"}
      (it "should add the stream"
        (let [stream-name "foo"]
          ;; route: streams-api/collection :post
          (doto $httpBackend
            (.. (expectPOST "/model/streams") (respond (constantly #js [200 stream-name]))))
          (let [p (.addStream app stream-name)]
            (.flush $httpBackend)
            (.. (js/expect p) (toBeResolvedWith stream-name))))))

    (describe {:doc ".connect"}
      (it "should open a websocket connection"
        (let [spy (.. (js/spyOn app "send")
                      -and
                      (returnValue "foo"))])
        (timbre/spy (.connect app))))

    #_
    (describe {:doc ".follow"}
      (describe {:doc "when not authenticated"}
        (it "should be rejected"
          (let [p (.follow app)]
            (.$digest $rootScope)
            (.. (js/expect p) (toBeRejected))))))

    (describe {:doc ".getUser"}
      (describe {:doc "when not authenticated"}
        (it "resolves to nil"
          (.. (js/spyOn app   "getUserId") -and (returnValue nil))
          (let [p (.getUser app)]
            (.$digest $rootScope)
            (.. (js/expect p) (toBeResolvedWith nil)))))
      (describe {:doc "when authenticated"}
        (it "returns that user"
          (let [Users (.inject app "Users")
                id "acct:foo@example.com"
                user #js {:_id id}]
            (.. (js/spyOn app   "getUserId") -and (returnValue id))
            (.. (js/spyOn Users "find")      -and (returnValue ($q #(% user))))
            (let [p (.getUser app)]
              (.$digest $rootScope)
              (.. (js/expect p) (toBeResolvedWith user))
              (.. (js/expect (.-find Users)) (toHaveBeenCalledWith id)))))))

    (describe {:doc ".invokeAction"}
      (it "sends a message"
        (let [model-name "user"
              action-name "delete"
              id "acct:user@example.com"]
          (.. (js/spyOn app "send") -and (returnValue ($q #(%))) )
          (let [p (.invokeAction app model-name action-name id)]
            (.$digest $rootScope)
            (.. (js/expect p) (toBeResolved))))))

    #_
    (describe {:doc ".login"}
      (describe {:doc "with valid credentials"}
        (it "returns successfully"
          (doto $httpBackend
            (.. (expectPOST "/main/login") (respond valid-login-response))
            (.. (whenGET "/status")        (respond #js {})))
          (let [username "test"
                password "test"
                p (.login app username password)]
            (.flush $httpBackend)
            (.$digest $rootScope)
            (.. (js/expect p) (toBeResolvedWith true)))))
      (describe {:doc "with invalid credentials"}
        (it "is rejected"
          (doto $httpBackend
            (.. (expectPOST "/main/login") (respond invalid-login-response)))
          (let [username "test"
                password "test"
                p (.login app username password)]
            (.flush $httpBackend)
            (.$digest $rootScope)
            (.. (js/expect p) (toBeRejected))))))))
