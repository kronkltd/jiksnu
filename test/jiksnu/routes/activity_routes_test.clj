(ns jiksnu.routes.activity-routes-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [contains every-checker fact future-fact =>]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [ring.mock.request :as mock])
  (:import jiksnu.model.Activity
           jiksnu.model.User))


(test-environment-fixture

 (fact "show-http-route"
   (fact "when the user is not authenticated"
     (fact "and the activity does not exist"
       (let [author (model.user/create (factory :local-user))
             activity (factory :activity)]
         (->> (str "/notice/" (:_id activity))
              (mock/request :get)
              response-for) =>
              (contains {:status 404})))

     (fact "and there are activities"
       (let [author (model.user/create (factory :local-user))
             activity (factory :activity {:author (:_id author)})
             created-activity (model.activity/create activity)]
         (->> (str "/notice/" (:_id created-activity))
              (mock/request :get)
              response-for) =>
              (every-checker
               (contains {:status 200})
               (fn [response]
                 (fact
                   (:body response) => (re-pattern (str (:_id created-activity)))))))))))
