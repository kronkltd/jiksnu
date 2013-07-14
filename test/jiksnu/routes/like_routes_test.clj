(ns jiksnu.routes.like-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.routes-helper :only [as-admin response-for]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=> contains]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.like :as model.like]
            [ring.mock.request :as req]))

(test-environment-fixture

 (future-context "delete html"
   (let [like (model.like/create (factory :like))]
     (-> (req/request :post (format "/likes/%s/delete" (:_id like)))
         as-admin response-for) =>
         (check [response]
           response => map?
           (:status response) => status/redirect?
           (:body response) => string?
           (get-in response [:headers "Content-Type"]) => "text/html"

           ;; TODO: use an exist test
           (model.like/fetch-by-id (:_id like)) => nil)))

 )
