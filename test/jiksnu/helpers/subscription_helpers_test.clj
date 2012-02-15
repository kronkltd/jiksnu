(ns jiksnu.helpers.subscription-helpers-test
  (:use (ciste [config :only [with-environment]])
        clj-factory.core
        jiksnu.test-helper
        jiksnu.helpers.subscription-helpers
        midje.sweet)
  (:require [clj-tigase.element :as element]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(test-environment-fixture

 (fact "subscriber-response-element"
   (fact "should"
     (let [user (model.user/create (factory User))
           subscribee (model.user/create (factory User))
           subscription (model.subscription/subscribe
                         (:_id user) (:_id subscribee))]
       (let [response (subscriber-response-element subscription)]
         response => element/element?))))

 (fact "subscribe-request"
   (fact "should"
     (let [user (model.user/create (factory User))
           subscribee (model.user/create (factory User))
           subscription (model.subscription/subscribe
                         (:_id user) (:_id subscribee))]
       (let [response (subscribe-request subscription)]
         response => element/element?)))))
