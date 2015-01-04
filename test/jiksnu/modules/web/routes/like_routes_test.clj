(ns jiksnu.modules.web.routes.like-routes-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.model.like :as model.like]
            [jiksnu.routes-helper :refer [as-admin response-for]]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [midje.sweet :refer [=> fact future-fact]]
            [ring.mock.request :as req]))

(test-environment-fixture

 (future-fact "delete html"
   (let [like (model.like/create (factory :like))
         url (format "/likes/%s/delete" (:_id like))]
     (let [response (-> (req/request :post url)
                        as-admin response-for)]
       response => map?
       (:status response) => status/redirect?
       (:body response) => string?
       (get-in response [:headers "Content-Type"]) => "text/html"

       ;; TODO: use an exist test
       (model.like/fetch-by-id (:_id like)) => nil

       )))

 )
