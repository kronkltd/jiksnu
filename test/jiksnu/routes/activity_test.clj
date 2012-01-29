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
            (jiksnu.model [user :as model.user])
            (jiksnu.actions [activity-actions :as actions.activity]
                            [user-actions :as actions.user])
            (ring.mock [request :as mock]))
  (:import (jiksnu.model Activity User)))

(with-environment :test
  (test-environment-fixture)

  (fact "show-http-route"
    
    (fact "when the user is not authenticated"
      (fact "and the activity does not exist"
        (let [author (actions.user/create (factory User))
              ch (channel)
              activity (factory Activity)]
          (session/with-user author
            (let [path (str "/notice/" (:_id activity))]
              (r/app ch (mock/request :get path))
              (let [response (wait-for-message ch 5000)]
                response => (contains {:status 404}))))))

      (fact "and there are activities"
        (let [author (actions.user/create (factory User))
              ch (channel)
              activity (factory Activity)
              created-activity (session/with-user author
                                 (actions.activity/post activity))
              path (str "/notice/" (:_id created-activity))]
          (r/app ch (mock/request :get path))
          (let [response (wait-for-message ch 5000)]
            response => (contains {:status 200})))))))
