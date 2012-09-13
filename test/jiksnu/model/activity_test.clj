(ns jiksnu.model.activity-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.session :only [with-user]]
        [jiksnu.model.activity :only [create create-validators get-author prepare-activity]]
        [midje.sweet :only [fact future-fact =>]]
        [validateur.validation :only [valid?]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(test-environment-fixture

 (fact "#'create"
   (fact "should create the activity"
     (create (factory :activity)) => model/activity?))
 
 (fact "#'prepare-activity"
   (fact "should return an activity"
     (let [user (actions.user/create (factory :local-user))]
       (with-user user
         (let [args (factory :activity)]
           (prepare-activity args) => #(valid? % create-validators))))))


 (fact "#'get-author"
   (let [user (actions.user/create (factory :local-user))
         activity (create (factory :activity {:author (:_id user)}))]
     (get-author activity) => user))

 )
