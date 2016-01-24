(ns jiksnu.actions.client-actions-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import jiksnu.model.Client))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact #'actions.client/create
  (let [params (factory :client)
        response (actions.client/create params)]
    (fact "should return a client"
      response => (partial instance? Client))))
