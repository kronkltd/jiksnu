(ns jiksnu.app.provider-methods-test
  (:require [cljs.test :refer-macros [async deftest is testing]]
            [jiksnu.app.provider-methods :as methods]
            jiksnu.main
            [taoensso.timbre :as timbre]))

(declare $http)
(declare $q)
(declare $rootScope)
(declare app)
(declare Users)
(declare $httpBackend)
(declare $httpParamSerializerJQLike)

(declare auth-data)
(declare auth-id)
(declare auth-user)
(declare auth-username)
(def auth-domain "example.com")

(defn valid-login-response
  [_method _url _data _headers _params]
  #js [204 nil #js {} "No-Content"])

(defn invalid-login-response
  [_method _url _data _headers _params]
  #js [409 nil #js {} "Unauthenticated"])

(defn activity-submit-response
  [activity-id]
  (fn [_method _url _data _headers _params]
    (let [body #js {:_id activity-id}
          headers #js {"Content-Type" "application/json"}]
      #js [200 body headers "OK"])))

(defn update-auth-data!
  ([]
   (set! auth-data #js {:user auth-username :domain auth-domain})
   (set! auth-id (str "acct:" auth-username "@" auth-domain))
   (set! auth-user #js {:_id auth-id :username auth-username :domain auth-domain}))
  ([new-username]
   (set! auth-username new-username)
   (update-auth-data!))
  ([new-username new-domain]
   (set! auth-domain new-domain)
   (update-auth-data! new-username)))

(js/describe "jiksnu.app.provider-methods"
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
               (set! Users _Users_))])))

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
        (js/describe "when given a valid id"
          (fn []
            (js/it "resolves its promise"
              (fn []

                (-> (.expectPOST $httpBackend "/model/activities")
                    (.respond valid-login-response))

                (let [target-id "foo"
                      p (methods/delete-stream $http target-id)]
                  (.$digest $rootScope)
                  (.. (js/expect p) (toBeResolved)))))))))

    (js/describe "get-user"
      (fn []
        (js/describe "when not authenticated"
          (fn []
            (js/it "resolves to nil"
              (fn []
                (let [p (methods/get-user $q Users auth-data)]
                  (.$digest $rootScope)

                  (-> (js/expect p) (.toBeResolvedWith nil)))))))

        (js/describe "when authenticated"
          (fn []
            (js/beforeEach
             (fn []
               (update-auth-data! "foo")))

            (js/it "returns that user"
              (fn []
                (let [user-url (str "/model/users/" auth-id)]

                  (-> (.expectGET $httpBackend user-url)
                      (.respond auth-user))

                  (.. (js/spyOn Users "find") -and (returnValue ($q #(% auth-user))))

                  (let [p (methods/get-user $q Users auth-data)]
                    (.$digest $rootScope)
                    (-> (js/expect p) (.toBeResolvedWith auth-user))
                    (.. (js/expect (.-find Users)) (toHaveBeenCalledWith auth-id))))))))))

    (js/describe "fetch-status"
      (fn []
        (js/it "Should request the status page"
          (fn []
            (-> (.expectGET $httpBackend "/status") (.respond auth-data))

            (let [p (methods/fetch-status $http)]
              (.$digest $rootScope)
              (-> (js/expect p) (.toBeResolved auth-data)))))))

    (js/describe "follow"
      (fn []
        (js/describe "when passing an invalid target user"
          (let [target nil]
            (fn []
              (js/it "should be rejected"
                (fn []
                  (let [p (methods/follow $q $http target)]
                    (.$digest $rootScope)
                    (-> (js/expect p) (.toBeRejected))))))))))

    (js/describe "following?"
      (fn []
        (js/describe "when authenticated"
          (fn []
            (js/beforeEach (fn [] (update-auth-data! "foo")))

            (js/describe "when the target is nil"
              (fn []
                (let [target nil]
                  (js/it "should be rejected"
                    (fn []
                      (let [p (methods/following? $q Users auth-data target)]
                        (-> (js/expect p) (.toBeRejected))))))))

            (js/describe "when the user is following the target"
              (fn []
                (let [target-username "bar"
                      target-domain "example.com"
                      target-id (str "acct:" target-username "@" target-domain)
                      target #js {:_id target-id}]

                  (js/it "should return truthy"
                    (fn []
                      (-> $httpBackend
                          (.expectGET (str "/model/users/" auth-id))
                          (.respond 200 auth-user))

                      (let [p (methods/following? $q Users auth-data target)]
                        (-> (js/expect p) (.toBeResolvedWith true))))))))))))

    (js/describe "login"
      (fn []
        (js/describe "with valid credentials"
          (fn []
            (js/it "returns successfully"
              (fn []
                (-> (.expectPOST $httpBackend "/main/login")
                    (.respond valid-login-response))

                (->  (.whenGET $httpBackend "/status")
                     (.respond auth-data))

                (let [auth-username "test"
                      auth-password "test"
                      p (methods/login
                         $http $httpParamSerializerJQLike
                         auth-username auth-password)]
                  (.flush $httpBackend)
                  (.$digest $rootScope)
                  (-> (js/expect p) (.toBeResolvedWith true)))))))

        (js/describe "with invalid credentials"
          (fn []
            (js/it "is rejected"
              (fn []
                (-> (.expectPOST $httpBackend "/main/login")
                    (.respond invalid-login-response))

                (let [auth-username "test"
                      auth-password "test"
                      p (methods/login $http $httpParamSerializerJQLike
                                       auth-username auth-password)]
                  (.flush $httpBackend)
                  (.$digest $rootScope)
                  (-> (js/expect p) (.toBeRejected)))))))))

    (js/describe "post"
      (fn []
        (js/it "should return the id of the posted entry"
          (fn []
            (let [activity-id "58cd9980f8dfa1002f2e1642"
                  activity #js {:_id activity-id :content "foo"}
                  pictures nil
                  p (methods/post $http activity pictures)]

              (-> (.expectPOST $httpBackend "/model/activities")
                  (.respond (activity-submit-response activity-id)))

              (.$digest $rootScope)
              (-> (js/expect p) (.toBeResolvedWith activity-id)))))))))
