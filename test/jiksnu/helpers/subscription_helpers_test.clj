(ns jiksnu.helpers.subscription-helpers-test
  (:use clj-factory.core
        clojure.test
        jiksnu.test-helper
        jiksnu.helpers.subscription-helpers
        midje.sweet)
  (:require [clj-tigase.element :as element]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(test-environment-fixture)

;; (deftest subscriber-response-element-test)

(fact "should"
  (let [user (model.user/create (factory User))
        subscribee (model.user/create (factory User))
        subscription (model.subscription/subscribe
                      (:_id user) (:_id subscribee))]
    (let [response (subscriber-response-element subscription)]
      (is (or (vector? response)
              (element/element? response))))))

;; (deftest subscribe-request-test)

(fact "should"
  (let [user (model.user/create (factory User))
        subscribee (model.user/create (factory User))
        subscription (model.subscription/subscribe
                      (:_id user) (:_id subscribee))]
    (let [response (subscribe-request subscription)]
      (is (or (vector? response)
              (element/element? response))))))
