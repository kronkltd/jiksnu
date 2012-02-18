(ns jiksnu.actions.user-actions-test
  (:use midje.sweet
        (jiksnu [test-helper :only [test-environment-fixture]])
        jiksnu.actions.user-actions

        )
  )

(test-environment-fixture

 (fact "#'create"
   (fact "when the params ar nil"
     (fact "should throw an exception"
       (create nil) =not=> (throws IllegalArgumentException)
       )))

 )
