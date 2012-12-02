(ns jiksnu.helpers.subscription-helpers-test
  (:use [jiksnu.test-helper :only [test-environment-fixture]]
        jiksnu.helpers.subscription-helpers
        midje.sweet)
  (:require [clj-tigase.element :as element]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.features-helper :as feature]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(test-environment-fixture

 (fact "subscriber-response-element"
   (let [subscription (existance/a-subscription-exists)]
     (subscriber-response-element subscription) => vector?))

 (fact "subscribe-request"
   (let [subscription (existance/a-subscription-exists)]
     (subscribe-request subscription) => vector?)))
