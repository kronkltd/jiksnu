(ns jiksnu.routes.stream-test
  (:use (ciste [config :only [with-environment]])
        (clj-factory [core :only [factory]])
        (jiksnu [test-helper :only [test-environment-fixture]]
                [routes-helper :only [response-for]])
        midje.sweet)
  (:require (jiksnu.model [activity :as model.activity]
                          [user :as model.user])
            (ring.mock [request :as mock])))

(test-environment-fixture
 
 (fact "index-http-route"
   (fact "and there are no activities"
     (model.activity/drop!)

     (fact "should return a sucessful response"
       (let [response (->> "/"
                           (mock/request :get)
                           response-for)]
         response => (contains {:status 200}))))

   (fact "and there are activities"
     (let [user (model.user/create (factory :user))]
       (dotimes [n 10]
         (model.activity/create (factory :activity {:author (:_id user)})))

       (let [response (->> "/"
                           (mock/request :get)
                           response-for)]
         response => (contains {:status 200}))))))
