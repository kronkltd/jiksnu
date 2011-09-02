(ns jiksnu.views.domain-views-test
  (:use clj-factory.core
        (clj-tigase [element :only (element?)]
                    [packet :only (packet?)])
        clojure.test
        midje.sweet
        (jiksnu core-test)
        jiksnu.views.domain-views)
  (:require (jiksnu.actions [domain-actions :as actions.domain]))
  (:import jiksnu.model.Domain))

(use-fixtures :each test-environment-fixture)

(deftest test-ping-request
  (fact
    (let [domain (actions.domain/create (factory Domain))]
      (ping-request domain) => (contains {:body element?}))
    ))
