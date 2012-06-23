(ns jiksnu.routes.stream-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.routes-helper :only [get-auth-cookie response-for]]
        [midje.sweet :only [fact future-fact => every-checker contains]])
  (:require [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [ring.mock.request :as mock]))

(test-environment-fixture
 
 (fact "public-timeline-http-route"
   (fact "when there are no activities"
     (model/drop-all!)

     (-> (mock/request :get "/")
         response-for) =>
         (every-checker
          (contains {:status 200})))

   (fact "when there are activities"
     (let [password (fseq :password)
           user (model.user/create (factory :user))]

       (actions.auth/add-password user password)
       
       (dotimes [n 10]
         (model.activity/create (factory :activity {:author (:_id user)})))
       
       (model.subscription/create (factory :subscription {:actor (:_id user)}))

       (fact "when the user is not authenticated"
         (-> (mock/request :get "/")
             response-for) =>
             (every-checker
              (contains {:status 200})))

       (fact "when the user is authenticated"
         (let [cookie-str (get-auth-cookie (:username user) password)]
           (-> (mock/request :get "/")
               (assoc-in [:headers "cookie"] cookie-str)
               response-for) =>
               (every-checker
                map?
                (contains {:status 200})
                (fn [response]
                  (let [body (h/html (:body response))]
                    (fact
                      body => (re-pattern (:first-name user))
                      body => #"authenticated")))))))))

 )
