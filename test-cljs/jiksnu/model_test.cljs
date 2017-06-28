(ns jiksnu.modules.core.model-test
  (:require jiksnu.main
            [taoensso.timbre :as timbre]))

(declare Users)
(declare $httpBackend)
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
        #js ["$httpBackend" "Users"
             (fn [_$httpBackend_ _Users_]
               (update-auth-data! nil "example.com")
               (set! Users _Users_)
               #_(doto $httpBackend
                 (.. (whenGET #"/templates/.*") (respond "<div></div>"))
                 (.. (whenGET "/status")        (respond #js {}))))])))

    (js/it "does the needful"
      (fn []
        (let [id "acct:test@example.com"
              user (.createInstance Users #js {:_id id})]
          (-> (.expectGET $httpBackend #"/model/.*")
              (.respond user))
          (let [p (.find Users id)]
            (.$digest $rootScope)
            (-> (js/expect p) (.toBeResolvedWith user))))))))
