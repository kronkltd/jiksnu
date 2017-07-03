(ns jiksnu.model-test
  (:require [cljs.test :refer-macros [async deftest is testing]]
            jiksnu.config
            jiksnu.main
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
(declare DS)
(declare subpageService)

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

(js/describe "Users"
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

    (js/it "does the needful"
      (fn []
        (let [id "acct:test@example.com"
              user (.createInstance Users #js {:_id id})]
          (let [user2 (.find Users id)]
            (timbre/infof "user2: %s" (js/JSON.stringify user2))))))))
