(ns jiksnu.providers-test
  (:require [cljs.test :refer-macros [async deftest is testing]]
            jiksnu.main
            [jiksnu.provider-methods :as methods]
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
                    (.. (js/expect p) (toBeResolved))))))))))))
