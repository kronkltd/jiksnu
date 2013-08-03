(ns jiksnu.actions.admin.auth-actions-test
  (:use [jiksnu.test-helper :only [context test-environment-fixture]]
        jiksnu.actions.admin.auth-actions
        midje.sweet))

(test-environment-fixture

 (context #'index
   (index) => map?)

 )
