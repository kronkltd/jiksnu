(ns jiksnu.routes.like-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.routes-helper :only [as-admin response-for]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact future-fact => every-checker contains]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.like :as model.like]
            [ring.mock.request :as mock]))

(test-environment-fixture

 (fact "delete html"
   (let [like (model.like/create (factory :like))]
     (-> (mock/request :post (format "/likes/%s/delete" (:_id like)))
         as-admin response-for) =>
         (every-checker
          map?
          (comp status/redirect? :status))
         (model.like/fetch-by-id (:_id like)) => nil))
 
 )
