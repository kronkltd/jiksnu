(ns jiksnu.model.authentication-mechanism-test
  (:use [jiksnu.model.like :only [fetch-all]]
        [jiksnu.test-helper :only [context test-environment-fixture]]
        [midje.sweet :only [=> fact]]))


(test-environment-fixture

 (fact #'fetch-all
   (fact "when not given any parameters"
     (fetch-all) => seq?)

   (fact "when passed an empty map"
     (fetch-all {}) => seq?))
 )
