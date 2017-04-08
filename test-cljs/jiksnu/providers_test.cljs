(ns jiksnu.providers-test
  (:require [cljs.test :refer-macros [async deftest is testing]]
            jiksnu.main
            [jiksnu.providers :as providers]
            [taoensso.timbre :as timbre]))

(declare app)
(declare $httpBackend)
(declare $q)
(declare $rootScope)

(deftest test-add-stream
  (let [app nil
        stream-name ""]
    (is (providers/add-stream app stream-name) nil)))

(defn valid-login-response
  [method url data headers params]
  #js [204 nil #js {} "No-Content"])

(defn invalid-login-response
  [method url data headers params]
  #js [409 nil #js {} "Unauthenticated"])

(js/describe "jiksnu.providers"
  (fn []
    (js/beforeEach (fn [] (js/module "jiksnu")))

    (js/beforeEach (fn [] (js/installPromiseMatchers)))

    (js/beforeEach
     (fn []
       (js/inject
        #js ["app" "$httpBackend" "$q" "$rootScope"
             (fn [_app_ _$httpBackend_ _$q_ _$rootScope_]
               (set! app _app_)
               (set! $httpBackend _$httpBackend_)
               (set! $q _$q_)
               (set! $rootScope _$rootScope_)
               (doto $httpBackend
                 (.. (whenGET #"/templates/.*") (respond "<div></div>"))
                 (.. (whenGET #"/model/.*")     (respond "{}"))))])))

    (js/afterEach (fn [] (.verifyNoOutstandingRequest $httpBackend)))

    (js/describe "app"
      (fn []
        (js/describe ".addStream"
          (fn []
            (js/it "should add the stream"
              (fn []
                (let [stream-name "foo"]
                  ;; route: streams-api/collection :post
                  (doto $httpBackend
                    (.. (expectPOST "/model/streams") (respond (constantly #js [200 stream-name]))))
                  (let [p (.addStream app stream-name)]
                    (.flush $httpBackend)
                    (.. (js/expect p) (toBeResolvedWith stream-name))))))))

        (js/describe ".connect"
          (fn []
            (js/it "should open a websocket connection"
              (fn []
                (let [spy (.. (js/spyOn app "send")
                              -and
                              (returnValue "foo"))])
                (timbre/spy (.connect app))))))

        (js/describe ".follow"
          (fn []
            (js/describe "when not authenticated"
              (fn []
                (js/it "should be rejected"
                  (fn []
                    (let [p (.follow app)]
                      (.$digest $rootScope)
                      (.. (js/expect p) (toBeRejected)))))))))

        (js/describe ".getUser"
          (fn []
            (js/describe "when not authenticated"
              (fn []
                (js/it "resolves to nil"
                  (fn []
                    (.. (js/spyOn app "getUserId") -and (returnValue nil))
                    (let [p (.getUser app)]
                      (.$digest $rootScope)
                      (.. (js/expect p) (toBeResolvedWith nil)))))))
            (js/describe "when authenticated"
              (fn []
                (js/it "returns that user"
                  (fn []
                    (let [Users (.inject app "Users")
                          id "acct:foo@example.com"
                          user #js {:_id id}]
                      (.. (js/spyOn app   "getUserId") -and (returnValue id))
                      (.. (js/spyOn Users "find")      -and (returnValue ($q #(% user))))
                      (let [p (.getUser app)]
                        (.$digest $rootScope)
                        (.. (js/expect p) (toBeResolvedWith user))
                        (.. (js/expect (.-find Users)) (toHaveBeenCalledWith id))))))))))

        (js/describe ".invokeAction"
          (fn []
            (js/it "sends a message"
              (fn []
                (let [model-name "user"
                      action-name "delete"
                      id "acct:user@example.com"]
                  (.. (js/spyOn app "send") -and (returnValue ($q #(%))) )
                  (let [p (.invokeAction app model-name action-name id)]
                    (.$digest $rootScope)
                    (.. (js/expect p) (toBeResolved))))))))

        (js/describe ".login"
          (fn []
            (js/describe "with valid credentials"
              (fn []
                (js/it "returns successfully"
                  (fn []
                    (doto $httpBackend
                      (.. (expectPOST "/main/login") (respond valid-login-response))
                      (.. (whenGET "/status")        (respond #js {})))
                    (let [username "test"
                          password "test"
                          p (.login app username password)]
                      (.flush $httpBackend)
                      (.$digest $rootScope)
                      (.. (js/expect p) (toBeResolvedWith true)))))))
            (js/describe "with invalid credentials"
              (fn []
                (js/it "is rejected"
                  (fn []
                    (doto $httpBackend
                      (.. (expectPOST "/main/login") (respond invalid-login-response)))
                    (let [username "test"
                          password "test"
                          p (.login app username password)]
                      (.flush $httpBackend)
                      (.$digest $rootScope)
                      (.. (js/expect p) (toBeRejected)))))))))))))
