(ns jiksnu.modules.admin.actions.group-actions-test
    (:require [jiksnu.modules.admin.actions.group-actions :refer [index]]
              [jiksnu.test-helper
               :refer [test-environment-fixture]]
              [midje.sweet :refer [=> fact]]))

(test-environment-fixture

 (fact #'index
   (let [response (index)]
     response => map?
     (:items response) => seq?
     (:totalRecords response) => zero?))

 )
