(ns jiksnu.model.domain-test
  (:use (ciste [config :only [with-environment]])
        clj-factory.core
        (clj-tigase [element :only (element?)]
                    [packet :only (packet?)])
        clojure.test
        midje.sweet
        (jiksnu test-helper)
        jiksnu.model.domain)
  (:require (jiksnu.actions [domain-actions :as actions.domain]))
  (:import jiksnu.model.Domain))


(test-environment-fixture
  
  (fact "ping-request"
    (fact "should return a ping packet"
      (let [domain (create (factory Domain))]
        (ping-request domain) => (contains {:body element?}))))

  (fact "pending-domains-key"
    (fact "should return a key name"
      (let [domain (create (factory Domain))]
        (pending-domains-key domain) => string?)))

  )
