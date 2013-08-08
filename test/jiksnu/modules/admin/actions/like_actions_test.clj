(ns jiksnu.modules.admin.actions.like-actions-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [jiksnu.modules.admin.actions.like-actions :only [delete index]]
        [midje.sweet :only [falsey => contains]])
  (:require [jiksnu.model.like :as model.like]))

(test-environment-fixture

 (context #'index

   (index) => (contains {:page 1
                         :totalRecords 0})

   (index {} {:page 2}) =>
   (check [response]
     (:page response) => 2
     (:totalRecords response) => 0))


 (future-context #'delete
   (let [like (model.like/create (factory :like))]
     (delete like)
     (model.like/fetch-by-id (:_id like)) => falsey

     )

   )
 )
