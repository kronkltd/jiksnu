(ns jiksnu.model.domain-test
  (:use clj-factory.core
        (clj-tigase [element :only (element?)]
                    [packet :only (packet?)])
        clojure.test
        midje.sweet
        (jiksnu core-test)
        jiksnu.model.domain)
  (:require (jiksnu.actions [domain-actions :as actions.domain]))
  (:import jiksnu.model.Domain))

(use-fixtures :once test-environment-fixture)

(deftest test-ping-request
  (fact "should return a ping packet"
    (let [domain (actions.domain/create (factory Domain))]
      (ping-request domain) => (contains {:body element?}))))
