(ns jiksnu.actions.conversation-actions-test
  (:use [ciste.debug :only [spy]]
        [jiksnu.actions.conversation-actions :only [index]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact =>]])
  )

(test-environment-fixture
 (fact "#'index"
   (fact "should return a page structure"
     (index) => map?
     )
   )
 )
