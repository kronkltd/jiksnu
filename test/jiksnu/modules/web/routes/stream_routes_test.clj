(ns jiksnu.modules.web.routes.stream-routes-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            jiksnu.modules.web.views.stream-views
            [jiksnu.test-helper :refer [check test-environment-fixture]]
            [jiksnu.routes-helper :refer [as-user response-for]]
            [midje.sweet :refer [=> fact]]
            [ring.mock.request :as req]))

(test-environment-fixture

 (fact "public-timeline-http-route"
   (fact "when there are no activities"
     (db/drop-all!)

     (-> (req/request :get "/")
         response-for) =>
         (check [response]
                response => map?
                (:status response) => status/success?))

   (fact "when there are activities"
     (let [user (mock/a-user-exists)]
       (dotimes [n 10]
         (mock/there-is-an-activity {:user user}))

       (fact "when the user is not authenticated"
         (-> (req/request :get "/")
             response-for) =>
             (check [response]
                    response => map?
                    (:status response) => status/success?
                    (:body response) => string?))

       (fact "when the user is authenticated"
         (-> (req/request :get "/")
             as-user
             response-for) =>
             (check [response]
                    response => map?
                    (:status response) => status/success?
                    (:body response) => string?))
       ))
   )

 (fact "user timeline"

   (fact "html"
     (let [user (mock/a-user-exists)]
       (dotimes [n 10]
         (mock/there-is-an-activity {:user user}))

       (-> (req/request :get (format "/%s" (:username user)))
           as-user response-for)) =>
           (check [response]
                  response => map?
                  (:status response) => status/success?
                  (:body response) => string?))
   )
 )
