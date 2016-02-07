(ns jiksnu.modules.web.routes.conversation-routes-test
  (:require [clojure.tools.logging :as log]
            [jiksnu.mock :as mock]
            [jiksnu.routes-helper :refer [as-user response-for]]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(facts "route: converation-api/activities-stream :get"
  (let [conversation (mock/a-conversation-exists)
        url (str "/model/conversations/" (:_id conversation) "/activities")
        request (req/request :get (log/spy :info url))]
    (util/inspect request)

    (mock/there-is-an-activity {:conversation conversation})

    (let [response (response-for request)]
      (util/inspect response)
      response =>
      (contains
       {:status 200}))))
