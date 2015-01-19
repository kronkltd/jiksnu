(ns jiksnu.modules.admin.actions.like-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [jiksnu.model.like :as model.like]
            [jiksnu.modules.admin.actions.like-actions :as actions.like]
            [midje.sweet :refer [=> contains fact falsey future-fact]]))

(test-environment-fixture

 (fact #'actions.like/index
   (actions.like/index) => (contains {:page 1 :totalItems 0})

   (let [response (actions.like/index {} {:page 2})]
     (:page response) => 2
     (:totalItems response) => 0))

 (future-fact #'actions.like/delete
   (let [like (model.like/create (factory :like))]
     (actions.like/delete like)
     (model.like/fetch-by-id (:_id like)) => falsey))
 )
