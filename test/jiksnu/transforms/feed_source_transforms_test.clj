(ns jiksnu.transforms.feed-source-transforms-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.service-actions :as actions.service]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.factory :refer [make-uri]]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.test-helper :as th]
            [jiksnu.transforms.feed-source-transforms :refer [set-domain]]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact #'set-domain
  (fact "when the source has a domain"
    (let [domain (mock/a-record-exists :domain)
          source (factory :feed-source {:domain (:_id domain)})]
      (set-domain source) => source))
  (fact "when the source does not have a domain"
    (let [domain (mock/a-record-exists :domain)
          url (make-uri (:_id domain))
          source (dissoc (factory :feed-source {:topic url}) :domain)]
      (set-domain source) => (contains {:domain (:_id domain)})

      (provided
        (actions.service/get-discovered anything nil nil) => {:_id (:_id domain)}))))


