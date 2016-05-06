(ns jiksnu.modules.admin.actions.group-actions-test
  (:require [jiksnu.modules.admin.actions.group-actions :refer [index]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.admin"])

(fact "#'index"
  (let [response (index)]
    response => map?
    (:items response) => seq?
    (:totalItems response) => zero?))
