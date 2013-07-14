(ns jiksnu.actions.admin.group-actions-test
    (:use [jiksnu.actions.admin.group-actions :only [index]]
          [jiksnu.test-helper :only [check context test-environment-fixture]]
          [midje.sweet :only [=> fact]] ))

(test-environment-fixture

 (context #'index
   (index) =>
   (check [response]
     response => map?
     (:items response) => seq?
     (:totalRecords items) => zero?))

 )
