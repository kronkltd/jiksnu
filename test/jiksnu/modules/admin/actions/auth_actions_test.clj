(ns jiksnu.modules.admin.actions.auth-actions-test
  (:use [jiksnu.test-helper :only [context test-environment-fixture]]
        jiksnu.modules.admin.actions.auth-actions
        midje.sweet))

(test-environment-fixture

 (context #'index
   (index) => map?)

 )
