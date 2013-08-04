(ns jiksnu.modules.admin.actions.subscription-actions-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.modules.admin.modules.admin.actions.subscription-actions :only [index delete]]
        [jiksnu.test-helper :only [context test-environment-fixture]]
        [midje.sweet :only [falsey => truthy]])
  (:require [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]))

(test-environment-fixture

 (context #'delete
   (let [subscription (mock/a-subscription-exists)]
     (model.subscription/fetch-by-id (:_id subscription)) => truthy
     (delete subscription)
     (model.subscription/fetch-by-id (:_id subscription)) => falsey))

 )
