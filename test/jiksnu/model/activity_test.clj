(ns jiksnu.model.activity-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.model :only [activity?]]
        [jiksnu.session :only [with-user]]
        jiksnu.model.activity
        midje.sweet)
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(test-environment-fixture

 (fact "#'create"
   (fact "should create the activity"
     (create (factory :activity)) => activity?))
 
 ;; (fact "#'prepare-activity"
 ;;   (fact "should return an activity"
 ;;     (let [user (model.user/create (factory User))]
 ;;       (with-user user
 ;;         (let [args (factory Activity)]
 ;;           (prepare-activity args) => activity?)))))

 )
