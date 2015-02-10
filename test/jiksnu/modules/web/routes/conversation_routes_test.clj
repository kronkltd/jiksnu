(ns jiksnu.modules.web.routes.conversation-routes-test
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(future-fact "index page (:viewmodel)"
  (->> "/main/conversations.viewmodel"
       (req/request :get)
       response-for) =>
       (contains {:status status/success?
                  :body string?}))
