(ns jiksnu.routes.stream-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.routes-helper :only [response-for]]
        [midje.sweet :only [fact future-fact => every-checker contains]])
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [ring.mock.request :as mock]))

(test-environment-fixture
 
 (fact "public-timeline-http-route"
   (fact "when there are no activities"
     (model.activity/drop!)

     (-> (mock/request :get "/")
         response-for) =>
         (every-checker
          (contains {:status 200})))

   (fact "when there are activities"
     (let [user (model.user/create (factory :user))]
       (dotimes [n 10]
         (model.activity/create (factory :activity {:author (:_id user)})))

       (-> (mock/request :get "/")
           response-for) =>
           (every-checker
            (contains {:status 200}))))))
