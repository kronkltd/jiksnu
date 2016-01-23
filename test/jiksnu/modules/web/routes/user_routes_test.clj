(ns jiksnu.modules.web.routes.user-routes-test
  (:require [clojurewerkz.support.http.statuses :as status]
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "index page"
  (let [url "/main/users"]
    (response-for (req/request :get url)) =>
    (contains {:status status/success?
               :body string?})))
