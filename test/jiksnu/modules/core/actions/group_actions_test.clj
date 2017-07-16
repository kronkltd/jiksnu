(ns jiksnu.modules.core.actions.group-actions-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.modules.core.actions.group-actions :as actions.group]
            [jiksnu.modules.core.model.group :as model.group]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import jiksnu.modules.core.model.Group))

(th/module-test ["jiksnu.modules.core"])

(fact "#'actions.group/create"
  (fact "when given valid options"
    (fact "and the group does not already exist"
      (model.group/drop!)
      (let [params (factory :group)]
        (actions.group/create params) => #(instance? Group %)))))
