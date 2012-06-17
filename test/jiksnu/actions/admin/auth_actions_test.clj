(ns jiksnu.actions.admin.auth-actions-test
  (:use [jiksnu.test-helper :only [test-environment-fixture]]
        jiksnu.actions.admin.auth-actions
        midje.sweet))

(test-environment-fixture
 (fact "#'index"
   (index) =>
   (every-checker
    map?
    )
   )
 )
