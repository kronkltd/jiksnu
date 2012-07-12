(ns jiksnu.model.authentication-mechanism-test
  (:use [jiksnu.model.like :only [fetch-all]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact => every-checker]]))


(test-environment-fixture

 (fact "#'fetch-all"
   (fact "when not given any parameters"
     (fetch-all) =>
     (every-checker
      seq?))

   (fact "when passed an empty map"
     (fetch-all {}) =>
     (every-checker
      seq?)))
 )
