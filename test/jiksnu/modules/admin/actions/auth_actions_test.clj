(ns jiksnu.modules.admin.actions.auth-actions-test
  (:require [jiksnu.test-helper :as th]
            [jiksnu.modules.admin.actions.auth-actions :as admin.actions.auth]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "#'admin.actions.auth/index"
  (admin.actions.auth/index) => map?)
