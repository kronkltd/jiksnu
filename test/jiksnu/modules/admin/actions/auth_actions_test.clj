(ns jiksnu.modules.admin.actions.auth-actions-test
  (:use [jiksnu.test-helper :only [test-environment-fixture]]
        jiksnu.modules.admin.actions.auth-actions
        [midje.sweet :only [=> fact]]))

(test-environment-fixture

 (fact #'index
   (index) => map?)

 )
