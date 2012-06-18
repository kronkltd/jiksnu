(ns jiksnu.actions.admin.like-actions-test
  (:use [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.actions.admin.like-actions :only [index]]
        [midje.sweet :only [fact => every-checker]]))

(test-environment-fixture

 (fact "#'index"

   (index) =>
   (every-checker
    #(fact (:page %) => 1)
    #(fact (:total-records %) => 0))

   (index {} {:page 2}) =>
   (every-checker
    #(fact (:page %) => 2)
    #(fact (:total-records %) => 0)))

 )
