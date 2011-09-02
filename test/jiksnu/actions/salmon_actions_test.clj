(ns jiksnu.actions.salmon-actions-test
  (:use clojure.test
        midje.sweet
        jiksnu.core-test
        jiksnu.actions.salmon-actions)
  (:require [clojure.java.io :as io]))

(use-fixtures :each test-environment-fixture)

(deftest test-process
  (testing "with a valid signature"
    (fact "should create the message"
      (let [request {:body (io/input-stream
                            (io/resource "envelope.xml"))}]
        (process request) => truthy)))
  (testing "with an invalid signature"
    (fact "should reject the message"

      )))
