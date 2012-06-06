(ns jiksnu.model.like-test
  (:use [jiksnu.model.like :only [fetch-all]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact =>]]))

(test-environment-fixture

 (fact "fetch-all"
   (fetch-all) => seq?)

 )
