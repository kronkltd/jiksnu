(ns jiksnu.actions.admin.group-actions-test
    (:use [jiksnu.actions.admin.group-actions :only [index]]
          [jiksnu.test-helper :only [context test-environment-fixture]]
          [midje.sweet :only [=> every-checker fact]] ))

(test-environment-fixture

 (context "#'index"
   (index) =>
   (every-checker
    map?
    (comp seq? :items)
    #(= 0 (:totalRecords %))))

 )
