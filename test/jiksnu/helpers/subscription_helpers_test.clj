(ns jiksnu.helpers.subscription-helpers-test
  (:use [ciste.config :only [with-environment]]
        clj-factory.core
        jiksnu.test-helper
        jiksnu.helpers.subscription-helpers
        midje.sweet)
  (:require [clj-tigase.element :as element]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.features-helper :as feature]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(test-environment-fixture

 (fact "subscriber-response-element"
   (fact "should"
     (let [user (feature/a-user-exists)
           subscribee (feature/a-user-exists)
           subscription (model.subscription/subscribe
                         (:_id user) (:_id subscribee))]
       (let [response (subscriber-response-element subscription)]
         response => vector?))))

 (fact "subscribe-request"
   (fact "should"
     (let [user (feature/a-user-exists)
           subscribee (feature/a-user-exists)
           subscription (model.subscription/subscribe
                         (:_id user) (:_id subscribee))]
       (let [response (subscribe-request subscription)]
         response => vector?)))))
