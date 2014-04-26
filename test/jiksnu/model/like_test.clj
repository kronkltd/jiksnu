(ns jiksnu.model.like-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.model.like :only [create delete fetch-by-id fetch-all]]
        [jiksnu.test-helper :only [context future-context test-environment-fixture]]
        [midje.sweet :only [falsey =>]]))

(test-environment-fixture

 (future-context #'delete
   (let [like (create (factory :like))]
     (delete like)
     (fetch-by-id (:_id like)) => falsey))

 (context #'fetch-all
   (fetch-all) => seq?)

 )
