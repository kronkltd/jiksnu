(ns jiksnu.modules.admin.actions.group-actions-test
    (:require [jiksnu.modules.admin.actions.group-actions :refer [index]]
              [jiksnu.test-helper
               :refer [check test-environment-fixture]]
              [midje.sweet :refer [=> fact]]))

(test-environment-fixture

 (fact #'index
   (index) =>
   (check [response]
     response => map?
     (:items response) => seq?
     (:totalRecords response) => zero?))

 )
