(ns jiksnu.model.authentication-mechanism-test
  (:use [jiksnu.model.like :only [fetch-all]]
        [jiksnu.test-helper :only [context test-environment-fixture]]
        [midje.sweet :only [fact => every-checker]]))


(test-environment-fixture

 (context #'fetch-all
   (context "when not given any parameters"
     (fetch-all) =>
     (every-checker
      seq?))

   (context "when passed an empty map"
     (fetch-all {}) =>
     (every-checker
      seq?)))
 )
