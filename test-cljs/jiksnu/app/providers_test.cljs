(ns jiksnu.app.providers-test
  (:require [jiksnu.app.providers :as providers]
            [jiksnu.app.provider-methods :as methods]
            jiksnu.app.services
            jiksnu.main
            [taoensso.timbre :as timbre]))

(declare $http)
(declare $httpBackend)
(declare $httpParamSerializerJQLike)
(declare $q)
(declare $rootScope)
(declare auth-data)
(declare auth-id)
(declare auth-user)
(declare app)
(def domain "example.com")
(declare username)
(declare Users)

(declare auth-data)
(declare auth-id)
(declare auth-user)

(defn update-auth-data!
  ([]
   (set! auth-data #js {:user username :domain domain})
   (set! auth-id (str "acct:" username "@" domain))
   (set! auth-user #js {:_id auth-id}))
  ([_username_]
   (set! username _username_)
   (update-auth-data!))
  ([_username_ _domain_]
   (set! domain _domain_)
   (update-auth-data! _username_)))

(js/describe "jiksnu.app.providers"
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

    (js/describe "get-websocket-connection"
      (fn []
        (js/it "should return a websocket connection"
          (fn []
            (let [response (providers/get-websocket-connection app)]
              (timbre/spy response))))))

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
                    (let [target nil
                          p (.follow app target)]
                      (.$digest $rootScope)
                      (-> (js/expect p) .toBeRejected))))))))

        (js/describe ".invokeAction"
          (fn []
            (js/it "sends a message"
              (fn []
                (let [model-name "user"
                      action-name "delete"
                      id "acct:user@example.com"]

                  (-> (js/spyOn app.connection "send") .-and (.returnValue ($q #(% true))))

                  (let [p (.invokeAction app model-name action-name id)]
                    (.$digest $rootScope)
                    (-> (js/expect p) .toBeResolved)))))))))))
