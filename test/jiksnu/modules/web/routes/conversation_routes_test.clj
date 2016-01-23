(ns jiksnu.modules.web.routes.conversation-routes-test
  (:require [ciste.sections.default :refer [full-uri]]
            [clj-factory.core :refer [fseq]]
            [jiksnu.mock :as mock]
            [jiksnu.routes-helper :refer [as-user response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(facts "route: converation-api/activities-stream :get"
  (let [conversation (mock/a-conversation-exists)
        url (str "/model/conversations/" (:_id conversation) "/activities")
        request (req/request :get url)]
    (response-for request) =>
    (contains
     {:status 200})))
