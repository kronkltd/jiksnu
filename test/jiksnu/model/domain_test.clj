(ns jiksnu.model.domain-test
  (:use clj-factory.core
        (clj-tigase [element :only (element?)]
                    [packet :only (packet?)])
        clojure.test
        midje.sweet
        (jiksnu test-helper)
        jiksnu.model.domain)
  (:require (jiksnu.actions [domain-actions :as actions.domain]))
  (:import jiksnu.model.Domain))

(test-environment-fixture)

(deftest test-drop!)

(deftest test-show!)

(deftest test-index)

(deftest test-create)

(deftest test-update)

(deftest test-delete)

(deftest test-find-or-create)

(deftest test-add-links)

(deftest test-set-field)

(deftest test-set-discovered)

(deftest test-ping-request
  (fact "should return a ping packet"
    (let [domain (actions.domain/create (factory Domain))]
      (ping-request domain) => (contains {:body element?}))))

(deftest test-pending-domains-key
  (fact "should return a key name"
    (let [domain (actions.domain/create (factory Domain))]
      (pending-domains-key domain) => string?)))

