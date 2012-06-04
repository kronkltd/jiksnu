(ns jiksnu.model.domain-test
  (:use [clj-factory.core :only [factory]]
        [clj-tigase.element :only [element?]]
        [clj-tigase.packet :only [packet?]]
        [midje.sweet :only [fact => contains every-checker]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.model.domain :only [create drop! ping-request pending-domains-key]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.actions.domain-actions :as actions.domain])
  (:import jiksnu.model.Domain))


(test-environment-fixture
 
 (fact "ping-request"
   (fact "should return a ping packet"
     (drop!)
     (let [domain (create (factory :domain))]
       (ping-request domain) => (contains {:body element?}))))

 (fact "pending-domains-key"
   (fact "should return a key name"
     (drop!)
     (let [domain (create (factory :domain))]
       (pending-domains-key domain) => string?)))

 (fact "#'create"
   (create (factory :domain)) =>
   (every-checker
    (partial instance? Domain)
    )
           
   )
 
 )
