(ns jiksnu.helpers.subscription-helpers-test
  (:use [ciste.config :only [with-environment]]
        clj-factory.core
        jiksnu.test-helper
        jiksnu.helpers.subscription-helpers
        midje.sweet)
  (:require [clj-tigase.element :as element]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(test-environment-fixture

 (fact "subscriber-response-element"
   (fact "should"
     (let [user (actions.user/create (factory :user))
           subscribee (actions.user/create (factory :user))
           subscription (model.subscription/subscribe
                         (:_id user) (:_id subscribee))]
       (let [response (subscriber-response-element subscription)]
         response => vector?))))

 (fact "subscribe-request"
   (fact "should"
     (let [user (actions.user/create (factory :user))
           subscribee (actions.user/create (factory :user))
           subscription (model.subscription/subscribe
                         (:_id user) (:_id subscribee))]
       (let [response (subscribe-request subscription)]
         response => vector?)))))
