(ns jiksnu.modules.admin.actions.like-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.test-helper :refer [check test-environment-fixture]]
            [jiksnu.model.like :as model.like]
            [jiksnu.modules.admin.actions.like-actions :refer [delete index]]
            [midje.sweet :refer [=> contains fact falsey future-fact]]))

(test-environment-fixture

 (fact #'index

   (index) => (contains {:page 1
                         :totalRecords 0})

   (index {} {:page 2}) =>
   (check [response]
     (:page response) => 2
     (:totalRecords response) => 0))


 (future-fact #'delete
   (let [like (model.like/create (factory :like))]
     (delete like)
     (model.like/fetch-by-id (:_id like)) => falsey

     )

   )
 )
