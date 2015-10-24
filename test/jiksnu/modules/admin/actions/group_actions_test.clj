(ns jiksnu.modules.admin.actions.group-actions-test
    (:require [jiksnu.modules.admin.actions.group-actions :refer [index]]
              [jiksnu.test-helper :as th]
              [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "#'index"
  (let [response (index)]
    response => map?
    (:items response) => seq?
    (:totalItems response) => zero?))
