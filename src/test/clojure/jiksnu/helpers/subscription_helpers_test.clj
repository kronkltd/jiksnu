(ns jiksnu.helpers.subscription-helpers-test
  (:use clj-factory.core
        clj-tigase.core
        clojure.test
        jiksnu.core-test
        jiksnu.helpers.subscription-helpers)
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(use-fixtures :each test-environment-fixture)

(deftest subscriber-response-element-test
  (testing "should"
    (let [user (model.user/create (factory User))
          subscribee (model.user/create (factory User))
          subscription (model.subscription/subscribe
                        (:_id user) (:_id subscribee))]
      (let [response (subscriber-response-element subscription)]
        (is (or (vector? response)
                    (element? response)))))))

(deftest subscribe-request-test
  (testing "should"
    (let [user (model.user/create (factory User))
          subscribee (model.user/create (factory User))
          subscription (model.subscription/subscribe
                        (:_id user) (:_id subscribee))]
      (let [response (subscribe-request subscription)]
        (is (or (vector? response)
                    (element? response)))))))
