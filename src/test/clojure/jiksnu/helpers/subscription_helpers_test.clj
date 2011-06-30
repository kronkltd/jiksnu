(ns jiksnu.helpers.subscription-helpers-test
  (:use clj-factory.core
        clj-tigase.core
        jiksnu.helpers.subscription-helpers)
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(deftest subscriber-response-element
  (testing "should"
    (let [user (model.user/create (factory User))
          subscribee (model.user/create (factory User))
          subscription (model.subscription/subscribe
                        (:_id user) (:_id subscribee))]
      (let [response (subscriber-response-element subscription)]
        (expect (or (vector? response)
                    (element? response)))))))

(deftest subscribe-request
  (testing "should"
    (let [user (model.user/create (factory User))
          subscribee (model.user/create (factory User))
          subscription (model.subscription/subscribe
                        (:_id user) (:_id subscribee))]
      (let [response (subscribe-request subscription)]
        (expect (or (vector? response)
                    (element? response)))))))
