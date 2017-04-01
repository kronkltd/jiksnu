(ns jiksnu.provider-methods-test
  (:require [cljs.test :refer-macros [async deftest is testing]]
            [jiksnu.provider-methods :as methods]
            [taoensso.timbre :as timbre]))

(declare $http)
(declare $q)
(declare $rootScope)
(declare app)
(declare Users)
(declare $httpBackend)
(declare $httpParamSerializerJQLike)
(declare username)
(def domain "example.com")

(declare auth-data)
(declare auth-id)
(declare auth-user)

(defn activity-submit-response
  [method url data headers params]
  #js [200 nil data "OK"])

(defn valid-login-response
  [method url data headers params]
  #js [204 nil #js {} "No-Content"])

(defn invalid-login-response
  [method url data headers params]
  #js [409 nil #js {} "Unauthenticated"])

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

(js/describe "jiksnu.provider-methods"
  (fn []

    (js/beforeEach
     (fn []
       (js/module "jiksnu")
       (js/installPromiseMatchers)
       (js/inject
        #js ["app" "$http" "$httpBackend" "$httpParamSerializerJQLike" "$q" "$rootScope"
             "Users"
             (fn [_app_ _$http_ _$httpBackend_ _$httpParamSerializerJQLike_
                  _$q_ _$rootScope_ _Users_]
               (update-auth-data! nil "example.com")

               (set! app _app_)
               (set! $http _$http_)
               (set! $httpBackend _$httpBackend_)
               (set! $httpParamSerializerJQLike _$httpParamSerializerJQLike_)
               (set! $q _$q_)
               (set! $rootScope _$rootScope_)
               (set! Users _Users_)
               (doto $httpBackend
                 (.. (whenGET #"/templates/.*") (respond "<div></div>"))
                 (.. (whenGET #"/model/.*")     (respond "{}"))))])))

    (js/afterEach (fn [] (.verifyNoOutstandingRequest $httpBackend)))

    (js/describe "add-stream"
      (fn []
        (js/it "should add the stream"
          (fn []
            (let [stream-name "foo"]
              ;; route: streams-api/collection :post
              (-> (.expectPOST $httpBackend "/model/streams")
                  (.respond (constantly #js [200 stream-name])))

              (let [p (methods/add-stream $http stream-name)]
                (.flush $httpBackend)
                (-> (js/expect p) (.toBeResolvedWith stream-name))))))))

    (js/describe "login"
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
                      p (methods/login $http $httpParamSerializerJQLike username password)]
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
                      p (methods/login $http $httpParamSerializerJQLike username password)]
                  (.flush $httpBackend)
                  (.$digest $rootScope)
                  (.. (js/expect p) (toBeRejected)))))))))

    (js/describe "post"
      (fn []
        (js/it "should submit the activity"
          (fn []
            (-> $httpBackend
                (.expectPOST #"/model/activities")
                (.respond activity-submit-response))

            (let [activity #js {}
                  pictures nil
                  p (methods/post $http activity pictures)]
              (-> (js/expect p) (.toBeResolved)))))))))
