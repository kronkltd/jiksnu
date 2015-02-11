(ns jiksnu.modules.web.routes.stream-routes-test
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            jiksnu.modules.web.views.stream-views
            [jiksnu.test-helper :as th]
            [jiksnu.routes-helper :refer [as-user response-for]]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(future-fact "public-timeline-http-route"
  (fact "when there are no activities"
    (db/drop-all!)

    (let [response (-> (req/request :get "/")
                       response-for)]
      response => map?
      (:status response) => status/success?))

  (fact "when there are activities"
    (let [user (mock/a-user-exists)]
      (dotimes [n 10]
        (mock/there-is-an-activity {:user user}))

      (fact "when the user is not authenticated"
        (let [response (-> (req/request :get "/")
                           response-for)]
          response => map?
          (:status response) => status/success?
          (:body response) => string?))

      (fact "when the user is authenticated"
        (let [response (-> (req/request :get "/")
                           as-user
                           response-for)]
          response => map?
          (:status response) => status/success?
          (:body response) => string?))
      ))
  )

(future-fact "user timeline"

  (fact "html"
    (let [user (mock/a-user-exists)]
      (dotimes [n 10]
        (mock/there-is-an-activity {:user user}))

      (let [response (-> (req/request :get (format "/%s" (:username user)))
                         as-user response-for)]
        response => map?
        (:status response) => status/success?
        (:body response) => string?)))
  )

