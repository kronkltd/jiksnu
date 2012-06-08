(ns jiksnu.actions.admin.group-actions-test
    (:use [jiksnu.test-helper :only [test-environment-fixture]]
          [jiksnu.actions.admin.group-actions :only [index]]
          [midje.sweet :only [fact future-fact => every-checker]] ))

(test-environment-fixture

 (fact "#'index"
   (index) =>
   (every-checker
    map?
    (comp seq? :items)
    #(= 0 (:total-records %))))

 )
