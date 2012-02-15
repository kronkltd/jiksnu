(ns jiksnu.routes.activity-test
  (:use (ciste [config :only [with-environment]]
               [debug :only [spy]])
        clj-factory.core
        clojure.test
        lamina.core
        jiksnu.test-helper
        midje.sweet)
  (:require (jiksnu [routes :as r]
                    [session :as session])
            (jiksnu.model [activity :as model.activity]
                          [user :as model.user])
            (jiksnu.actions [activity-actions :as actions.activity]
                            [user-actions :as actions.user])
            (ring.mock [request :as mock]))
  (:import (jiksnu.model Activity User)))


(defn response-for
  "Run a request against the main handler and wait for the response"
  ([request] (response-for request 5000))
  ([request timeout]
     (let [ch (channel)]
       (r/app ch request)
       (wait-for-message ch timeout))))

(test-environment-fixture

 (fact "show-http-route"
   (fact "when the user is not authenticated"
     (fact "and the activity does not exist"
       (let [author (model.user/create (factory User))
             activity (factory Activity)
             response (->> (str "/notice/" (:_id activity))
                           (mock/request :get)
                           response-for)]
         
         (fact "should return a not found"
           response => (contains {:status 404}))))

     (fact "and there are activities"
       (let [author (model.user/create (factory User))
             activity (factory Activity {:author (:_id author)})
             created-activity (model.activity/create activity)
             response (->> (str "/notice/" (:_id created-activity))
                           (mock/request :get)
                           response-for)]

         (fact "Should be a valid response"
           response => (contains {:status 200}))

         (fact "should contain the id of the activity"
           (:body response) => (re-pattern (str (:_id created-activity)))) )))))
