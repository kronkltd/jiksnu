(ns jiksnu.providers-test
  (:require [cljs.test :refer-macros [async deftest is testing]]
            jiksnu.main
            [jiksnu.providers :as providers]
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

(defn valid-login-response
  [method url data headers params]
  #js [204 nil #js {} "No-Content"])

(defn invalid-login-response
  [method url data headers params]
  #js [409 nil #js {} "Unauthenticated"])

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
                      (update-auth-data! "foo" "example.com")
                      (set! app.data auth-data)
                      (-> $httpBackend
                          (.expectGET (str "/model/users/" id))
                          (.respond user))
                      (let [p (.getUser app)]
                        (.$digest $rootScope)
                        (.. (js/expect p) (toBeResolvedWith user))))))))))

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
