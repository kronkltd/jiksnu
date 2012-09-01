(ns jiksnu.actions.like-actions-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.actions.like-actions :only [delete show]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact falsey future-fact => every-checker contains]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.like :as model.like]
            [ring.mock.request :as mock]))

(test-environment-fixture

 (fact "#'show"
   (let [tag-name (fseq :word)]
     (show tag-name) =>
     (every-checker
      seq?)))

 (fact "#'delete"
   (let [like (model.like/create (factory :like))]
     (delete like)
     (model.like/fetch-by-id (:_id like)) => falsey
     )
   )
 
 )
