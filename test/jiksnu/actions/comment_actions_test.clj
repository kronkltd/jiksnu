(ns jiksnu.actions.comment-actions-test
  (:use [ciste.config :only [with-environment]]
        [clj-factory.core :only [factory]]
        midje.sweet
        jiksnu.test-helper
        [jiksnu.session :only (with-user)]
        jiksnu.actions.comment-actions)
  (:require [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))


(test-environment-fixture

 (fact "#'fetch-comments"
   (fact "when the activity exists"
     (fact "and there are no comments"
       (fact "should return an empty sequence"
         (let [actor (model.user/create (factory User))]
           (with-user actor
             (let [activity (actions.activity/create (factory Activity))
                   [_ comments] (fetch-comments activity)]
               comments => empty?)))))))
 )
