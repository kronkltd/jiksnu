(ns jiksnu.actions.salmon-actions-test
  (:use clojure.test
        midje.sweet
        jiksnu.actions.salmon-actions))

(deftest test-process
  (testing "with a valid signature"
    (fact "should create the message"
      (let [request {:body ""}]
        (process request) => truthy)))
  (testing "with an invalid signature"
    (fact "should reject the message"

      )))
