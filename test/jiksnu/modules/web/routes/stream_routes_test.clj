(ns jiksnu.modules.web.routes.stream-routes-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.data.json :as json]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            jiksnu.modules.web.routes.stream-routes
            jiksnu.modules.web.views.stream-views
            [jiksnu.test-helper :as th]
            [jiksnu.routes-helper :refer [as-user response-for]]
            [midje.sweet :refer :all]
            [puget.printer :as puget]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "route: streams-api/collection :post"
  (let [params {:name (fseq :word)}
        actor (mock/a-user-exists)
        request (-> (req/request :post "/model/streams")
                    (req/body (json/json-str params))
                    (req/content-type "application/json")
                    (as-user actor))]
    (puget/cprint request)
    (let [response (response-for request)]
      response => (contains {:status 303})
      (puget/cprint response)
      #_(let [location (get-in response [:headers "Location"])
            request2 (req/request :get location)]
        (some-> request2 response-for :body json/read-str) =>
        (contains {"name" (:name params)
                   "owner" (:_id actor)})))))

(future-fact "public-timeline-http-route"
  (fact "when there are no activities"
    (db/drop-all!)

    (-> (req/request :get "/")
        response-for) =>
        (contains {:status status/success?}))

  (fact "when there are activities"
    (let [user (mock/a-user-exists)]
      (dotimes [n 10]
        (mock/there-is-an-activity {:user user}))

      (fact "when the user is not authenticated"
        (-> (req/request :get "/")
            response-for) =>
            (contains {:status status/success?
                       :body string?}))

      (fact "when the user is authenticated"
        (-> (req/request :get "/")
            as-user response-for) =>
            (contains {:status status/success?
                       :body string?})))))

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
