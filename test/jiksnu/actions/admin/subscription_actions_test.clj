(ns jiksnu.actions.admin.subscription-actions-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.actions.admin.subscription-actions :only [index delete]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [every-checker fact falsey future-fact => truthy]])
  (:require [jiksnu.existance-helpers :as existance]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]))

(test-environment-fixture

 (fact "#'delete"
   (let [subscription (existance/a-subscription-exists)]
     (model.subscription/fetch-by-id (:_id subscription)) => truthy
     (delete subscription)
     (model.subscription/fetch-by-id (:_id subscription)) => falsey))

 )
