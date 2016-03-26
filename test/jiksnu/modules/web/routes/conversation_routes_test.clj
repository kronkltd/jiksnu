(ns jiksnu.modules.web.routes.conversation-routes-test
  (:require [jiksnu.mock :as mock]
            [jiksnu.routes-helper :refer [as-user response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(facts "route: converation-api/activities-stream :get"
  (let [conversation (mock/a-conversation-exists)
        url (str "/model/conversations/" (:_id conversation) "/activities")
        request (req/request :get url)]
    (mock/there-is-an-activity {:conversation conversation})
    (let [response (response-for request)]
      response => (contains {:status 200}))))
