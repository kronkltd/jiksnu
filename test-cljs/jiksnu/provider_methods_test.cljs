(ns jiksnu.provider-methods-test
  (:require [cljs.test :refer-macros [async deftest is testing]]
            jiksnu.main
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

(defn valid-login-response
  [method url data headers params]
  #js [204 nil #js {} "No-Content"])

(defn invalid-login-response
  [method url data headers params]
  #js [409 nil #js {} "Unauthenticated"])

(defn activity-submit-response
  [activity-id]
  (fn [method url data headers params]
    (let [body #js {:_id activity-id}
          headers #js {"Content-Type" "application/json"}]
      #js [200 body headers "OK"])))

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
                 (.. (whenGET #"/model/.*")     (respond #js {}))
                 (.. (whenGET "/status")        (respond #js {}))))])))

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

    (js/describe "connect"
      (fn []
        (js/it "should open a websocket connection"
          (fn []
            (let [spy (.. (js/spyOn app "send")
                          -and
                          (returnValue "foo"))])
            (timbre/spy (.connect app))))))

    (js/describe "delete-stream"
      (fn []
        (js/it "resolves"
          (fn []
            (doto $httpBackend
              (.. (expectPOST "/model/activities") (respond valid-login-response)))

            (let [target-id "foo"
                  p (methods/delete-stream $http target-id)]
              (.$digest $rootScope)
              (.. (js/expect p) (toBeResolved)))))))

    (js/describe "get-user"
      (fn []
        (js/describe "when not authenticated"
          (fn []
            (js/it "resolves to nil"
              (fn []
                (.. (js/spyOn app "getUserId") -and (returnValue nil))
                (let [auth-data #js {}
                      p (methods/get-user $q Users auth-data)]
                  (.$digest $rootScope)
                  (.. (js/expect p) (toBeResolvedWith nil)))))))

        (js/describe "when authenticated"
          (fn []
            (js/it "returns that user"
              (fn []
                (let [username "foo"
                      domain "example.com"
                      data #js {:user username :domain domain}
                      id (str "acct:" username "@" domain)
                      user #js {:_id id}]
                  (.. (js/spyOn Users "find") -and (returnValue ($q #(% user))))
                  (let [p (methods/get-user $q Users data)]
                    (.$digest $rootScope)
                    (.. (js/expect p) (toBeResolvedWith user))
                    (.. (js/expect (.-find Users)) (toHaveBeenCalledWith id))))))))))

    (js/describe "fetch-status"
      (fn []
        (js/it "Should request the status page"
          (fn []
            (let [data #js {}]
              (.. $httpBackend (expectGET "/status") (respond data))
              (let [p (methods/fetch-status $http)]
                (.$digest $rootScope)
                (.. (js/expect p) (toBeResolved data))))))))

    (js/describe "follow"
      (fn []
        (js/describe "when not passing an invalid object"
          (fn []
            (js/it "should be rejected"
              (fn []
                (let [target nil
                      p (methods/follow $q $http target)]
                  (.$digest $rootScope)
                  (.. (js/expect p) (toBeRejected)))))))))

    (js/describe "following?"
      (fn []
        (let [username "foo"
              domain "example.com"
              data #js {:user username :domain domain}]
          (js/describe "when the is nil"
            (fn []
              (js/it "should be rejected"
                (fn []
                  (let [target nil
                        p (methods/following? $q Users data target)]
                    (.. (js/expect p) (toBeRejected)))))))

          (js/describe "when the user is following the target"
            (fn []
              (js/it "should return truthy"
                (fn []
                  (let [target #js {:_id "acct:bar@example.com"}
                        p (methods/following? $q Users data target)]

                    (.. (js/expect p) (toBeResolvedWith true)))))))

          (js/describe "when the user is not following the target"
            (fn [])))))

    (js/describe "login"
      (fn []
        (js/describe "with valid credentials"
          (fn []
            (js/it "returns successfully"
              (fn []
                (doto $httpBackend
                  (.. (expectPOST "/main/login") (respond valid-login-response))
                  (.. (whenGET "/status")        (respond #js {})))
                (let [auth-username "test"
                      auth-password "test"
                      p (methods/login
                         $http $httpParamSerializerJQLike
                         auth-username auth-password)]
                  (.flush $httpBackend)
                  (.$digest $rootScope)
                  (.. (js/expect p) (toBeResolvedWith true)))))))

        (js/describe "with invalid credentials"
          (fn []
            (js/it "is rejected"
              (fn []
                (doto $httpBackend
                  (.. (expectPOST "/main/login") (respond invalid-login-response)))
                (let [auth-username "test"
                      auth-password "test"
                      p (methods/login $http $httpParamSerializerJQLike
                                       auth-username auth-password)]
                  (.flush $httpBackend)
                  (.$digest $rootScope)
                  (.. (js/expect p) (toBeRejected)))))))))

    (js/describe "post"
      (fn []
        (js/it "should return the id of the posted entry"
          (fn []
            (let [activity-id "58cd9980f8dfa1002f2e1642"
                  activity #js {:_id activity-id :content "foo"}
                  pictures nil
                  p (methods/post $http activity pictures)]

              (-> $httpBackend
                  (.expectPOST #"/model/activities")
                  (.respond (activity-submit-response activity-id)))

              (.$digest $rootScope)
              (.. (js/expect p) (toBeResolvedWith activity-id)))))))))
