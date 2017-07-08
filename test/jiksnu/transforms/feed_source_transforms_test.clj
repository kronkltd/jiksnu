(ns jiksnu.transforms.feed-source-transforms-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.modules.core.actions.service-actions :as actions.service]
            [jiksnu.mock :as mock]
            [jiksnu.modules.core.factory :as f]
            [jiksnu.test-helper :as th]
            [jiksnu.transforms.feed-source-transforms :refer [set-domain]]
            [midje.sweet :refer :all]))

(th/module-test ["jiksnu.modules.core"])

(facts "#'set-domain"
  (fact "when the source has a domain"
    (let [domain (mock/a-record-exists :domain)
          source (factory :feed-source {:domain (:_id domain)})]
      (set-domain source) => source))
  (fact "when the source does not have a domain"
    (let [domain (mock/a-record-exists :domain)
          url (f/make-uri (:_id domain))
          source (dissoc (factory :feed-source {:topic url}) :domain)]
      (set-domain source) => (contains {:domain (:_id domain)})

      (provided
       (actions.service/get-discovered anything nil nil) => {:_id (:_id domain)}))))
