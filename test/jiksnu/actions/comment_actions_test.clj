(ns jiksnu.actions.comment-actions-test
  (:use [ciste.config :only [with-environment]]
        [clj-factory.core :only [factory]]
        midje.sweet
        jiksnu.test-helper
        [jiksnu.session :only (with-user)]
        jiksnu.actions.comment-actions)
  (:require [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model.user :as model.user]))


(test-environment-fixture

 (fact "#'fetch-comments"
   (fact "when the activity exists"
     (fact "and there are no comments"
       (let [actor (mock/a-user-exists)
             activity (mock/there-is-an-activity)
             [_ comments] (fetch-comments activity)]
         comments => empty?))))
 )
