(ns jiksnu.model.activity-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.session :only [with-user]]
        [jiksnu.model.activity :only [create create-validators get-author]]
        [midje.sweet :only [fact future-fact =>]]
        [validateur.validation :only [valid?]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(test-environment-fixture

 (fact "#'create"
   (fact "should create the activity"
     (let [feed-source (existance/a-feed-source-exists)
           activity (actions.activity/prepare-create
                     (factory :activity {:update-source (:_id feed-source)}))]
       (create activity) => model/activity?)))
 
 ;; (fact "#'prepare-activity"
 ;;   (fact "should return an activity"
 ;;     (let [user (existance/a-user-exists)]
 ;;       (with-user user
 ;;         (let [args (factory :activity)]
 ;;           (prepare-activity args) => #(valid? % create-validators))))))


 ;; (fact "#'get-author"
 ;;   (let [user (existance/a-user-exists)
 ;;         activity (existance/there-is-an-activity {:user user})]
 ;;     (get-author activity) => user))

 )
