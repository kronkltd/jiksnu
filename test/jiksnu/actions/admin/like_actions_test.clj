(ns jiksnu.actions.admin.like-actions-test
  (:use [ciste.debug :only [spy]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.actions.admin.like-actions :only [index]]
        [midje.sweet :only [fact => every-checker]]))

(test-environment-fixture

 (fact "#'index"

   (spy (index))
   => (every-checker
       #(= 1 (:page %))
       #(= 0 (:total-records %))
       )

   (spy (index {:page "2"}))
   => (every-checker
       #(= 2 (:page %))
       #(= 0 (:total-records %))
       )

   
   )
 
 )
