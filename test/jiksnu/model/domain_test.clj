(ns jiksnu.model.domain-test
  (:use [clj-factory.core :only [factory]]
        [midje.sweet :only [=> contains every-checker fact]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.model.domain :only [create drop! ping-request
                                    pending-domains-key]])
  (:require [clj-tigase.element :as e]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model])
  (:import jiksnu.model.Domain))

(test-environment-fixture

 (fact "#'ping-request"
   (drop!)
   (let [domain (mock/a-domain-exists)]
     (ping-request domain) => (contains {:body e/element?})))

 (fact "pending-domains-key"
   (fact "should return a key name"
     (drop!)
     (let [domain (mock/a-domain-exists)]
       (pending-domains-key domain) => string?)))

 (fact "#'create"
   (create (actions.domain/prepare-create (factory :domain))) =>
   (every-checker
    (partial instance? Domain)))

 )
