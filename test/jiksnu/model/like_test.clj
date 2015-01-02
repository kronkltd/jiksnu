(ns jiksnu.model.like-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.model.like :as model.like]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [midje.sweet :refer [=> fact falsey future-fact]]))

(test-environment-fixture

 (future-fact #'model.like/delete
   (let [like (model.like/create (factory :like))]
     (model.like/delete like)
     (model.like/fetch-by-id (:_id like)) => falsey))

 (fact #'model.like/fetch-all
   (model.like/fetch-all) => seq?)

 )
