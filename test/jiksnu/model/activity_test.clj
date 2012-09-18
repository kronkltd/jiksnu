(ns jiksnu.model.activity-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.session :only [with-user]]
        [jiksnu.model.activity :only [create create-validators get-author prepare-activity]]
        [midje.sweet :only [fact future-fact =>]]
        [validateur.validation :only [valid?]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(test-environment-fixture

 (fact "#'create"
   (fact "should create the activity"
     (let [feed-source (feature/a-feed-source-exists)
           activity (prepare-activity (factory :activity {:update-source (:_id feed-source)}))]
       (create activity) => model/activity?)))
 
 (fact "#'prepare-activity"
   (fact "should return an activity"
     (let [user (feature/a-user-exists)]
       (with-user user
         (let [args (factory :activity)]
           (prepare-activity args) => #(valid? % create-validators))))))


 (fact "#'get-author"
   (let [user (feature/a-user-exists)
         activity (feature/there-is-an-activity {:user user})]
     (get-author activity) => user))

 )
