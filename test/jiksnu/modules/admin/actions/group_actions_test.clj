(ns jiksnu.modules.admin.actions.group-actions-test
    (:use [jiksnu.modules.admin.actions.group-actions :only [index]]
          [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
          [midje.sweet :only [=>]] ))

(test-environment-fixture

 (context #'index
   (index) =>
   (check [response]
     response => map?
     (:items response) => seq?
     (:totalRecords response) => zero?))

 )
