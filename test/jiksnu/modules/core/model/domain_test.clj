(ns jiksnu.modules.core.model.domain-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.modules.core.actions.domain-actions :as actions.domain]
            [jiksnu.mock :as mock]
            [jiksnu.modules.core.model.domain :as model.domain]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import jiksnu.modules.core.model.Domain))

(th/module-test ["jiksnu.modules.core"])

(facts "#'model.domain/statusnet-url"
  (let [domain (mock/a-domain-exists)]
    (model.domain/statusnet-url domain) => string?))

(facts "#'model.domain/create"
  (let [params (actions.domain/prepare-create (factory :domain))]
    (model.domain/create params) => #(instance? Domain %)))

(facts "#'model.domain/get-xrd-url"
  (fact "when the domain doesn't exist"
    (let [domain nil
          uri "acct:foo@example.com"]
      (model.domain/get-xrd-url domain uri) => nil?)))
