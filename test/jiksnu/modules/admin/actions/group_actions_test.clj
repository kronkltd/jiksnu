(ns jiksnu.modules.admin.actions.group-actions-test
    (:require [jiksnu.modules.admin.actions.group-actions :refer [index]]
              [jiksnu.test-helper
               :refer [check context future-context test-environment-fixture]]
              [midje.sweet :refer [=>]]))

(test-environment-fixture

 (context #'index
   (index) =>
   (check [response]
     response => map?
     (:items response) => seq?
     (:totalRecords response) => zero?))

 )
