(ns jiksnu.actions.domain-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.factory :as factory]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.ops :as ops]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import jiksnu.model.Domain
           nu.xom.Document))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "#'actions.domain/create"
  (fact "when given valid options"
    (fact "and the domain does not already exist"
      (model.domain/drop!)
      (let [options (actions.domain/prepare-create {:_id (fseq :domain)})]
        (actions.domain/create options) => model/domain?))
    ;; TODO: already exists
    )
  ;; TODO: invalid options
  )

(fact "#'actions.domain/delete"

  ;; There is no reason this shouldn't be a success
  (fact "when the domain does not exist"
    (model.domain/drop!)
    (let [domain (factory :domain {:_id (fseq :domain)})]
      (actions.domain/delete domain) => nil?))

  (fact "when the domain exists"
    (let [domain (mock/a-domain-exists)]
      (fact "should return the domain"
        (actions.domain/delete domain) => domain)
      (fact "should be deleted"
        (model.domain/fetch-by-id (:_id domain)) => nil?))))

(fact "#'actions.domain/show"
  (actions.domain/show .domain.) => .domain.)
