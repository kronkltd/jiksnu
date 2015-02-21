(ns jiksnu.model.like-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.model.like :as model.like]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(future-fact "#'model.like/delete"
  (let [like (model.like/create (factory :like))]
    (model.like/delete like)
    (model.like/fetch-by-id (:_id like)) => falsey))

(fact "#'model.like/fetch-all"
  (model.like/fetch-all) => seq?)


