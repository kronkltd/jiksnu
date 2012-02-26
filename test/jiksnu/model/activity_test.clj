(ns jiksnu.model.activity-test
  (:use (ciste [config :only [with-environment]]
               [debug :only [spy]])
        (clj-factory [core :only [factory]])
        (jiksnu test-helper
                [model :only [activity?]]
                [session :only [with-user]]
                )
        jiksnu.model.activity
        midje.sweet)
  (:require (jiksnu.actions [user-actions :as actions.user]))
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(test-environment-fixture

 (fact "prepare-activity"
   (fact "should return an activity"
     (let [user (actions.user/create (factory User))]
       (with-user user
         (let [args (factory Activity)]
           (prepare-activity args) => activity?)))))

 )
