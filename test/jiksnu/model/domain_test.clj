(ns jiksnu.model.domain-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.domain :refer [create drop! get-xrd-url]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import jiksnu.model.Domain))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact #'create
  (let [params (actions.domain/prepare-create (factory :domain))]
    (create params) => (partial instance? Domain)))

(fact #'get-xrd-url
  (fact "when the domain doesn't exist"
    (get-xrd-url nil "acct:foo@example.com") => nil?))


