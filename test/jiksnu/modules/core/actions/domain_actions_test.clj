(ns jiksnu.modules.core.actions.domain-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.modules.core.actions.domain-actions :as actions.domain]
            [jiksnu.mock :as mock]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import jiksnu.model.Domain))

(th/module-test ["jiksnu.modules.core"])

(fact "#'actions.domain/create"
  (fact "when given valid options"
    (fact "and the domain does not already exist"
      (model.domain/drop!)
      (let [options (actions.domain/prepare-create {:_id (fseq :domain)})]
        (actions.domain/create options) =>
        (partial instance? Domain)))))

(fact "#'actions.domain/delete"

  ;; There is no reason this shouldn't be a success
  (fact "when the domain does not exist"
    (model.domain/drop!)
    (let [domain (factory :domain {:_id (fseq :domain)})]
      (actions.domain/delete domain) => nil?))

  (fact "when the domain exists"
    (let [domain (mock/a-domain-exists)
          id (:_id domain)]
      ;; Returns the domain, if deleted
      (actions.domain/delete domain) => domain

      ;; Should be deleted
      (model.domain/fetch-by-id id) => nil?)))

(fact "#'actions.domain/show"
  (actions.domain/show .domain.) => .domain.)
