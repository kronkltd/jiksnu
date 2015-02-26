(ns jiksnu.modules.web.routes.like-routes-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.model.like :as model.like]
            [jiksnu.routes-helper :refer [as-admin response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(future-fact "delete html"
  (let [like (model.like/create (factory :like))
        url (format "/likes/%s/delete" (:_id like))]
    (-> (req/request :post url)
        as-admin response-for) =>
        (contains {:status status/redirect?
                   :headers (contains {"Content-Type" "text/html"})
                   :body string?})

        (model.like/fetch-by-id (:_id like)) => nil))
