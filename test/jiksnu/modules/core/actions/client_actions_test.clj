(ns jiksnu.modules.core.actions.client-actions-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.modules.core.actions.client-actions :as actions.client]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import jiksnu.model.Client))

(th/module-test ["jiksnu.modules.core"])

(fact #'actions.client/create
  (let [params (factory :client)
        response (actions.client/create params)]
    (fact "should return a client"
      response => (partial instance? Client))))
