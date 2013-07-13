(ns jiksnu.actions.admin.like-actions-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.test-helper :only [context future-context test-environment-fixture]]
        [jiksnu.actions.admin.like-actions :only [delete index]]
        [midje.sweet :only [fact future-fact every-checker falsey => contains]])
  (:require [jiksnu.model.like :as model.like]))

(test-environment-fixture

 (context #'index

   (index) => (contains {:page 1
                         :totalRecords 0})

   (index {} {:page 2}) =>
   (every-checker
    #(fact (:page %) => 2)
    #(fact (:totalRecords %) => 0)))


 (future-context #'delete
   (let [like (model.like/create (factory :like))]
     (delete like)
     (model.like/fetch-by-id (:_id like)) => falsey

     )

   )
 )
