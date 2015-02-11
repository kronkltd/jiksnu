(ns jiksnu.modules.admin.routes.subscription-routes-test
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.mock :as mock]
            [jiksnu.routes-helper :refer [as-admin response-for]]
            [jiksnu.test-helper :as th]
            [hiccup.core :as h]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

;; TODO: This is a better test for the view
(future-fact "delete"
  (let [subscription (mock/a-subscription-exists)]
    (let [url (str "/admin/subscriptions/" (:_id subscription) "/delete")
          response (-> (req/request :post url)
                       as-admin
                       response-for)]

      (fact "returns a map"
        response => map?)

      (fact "returns a successful response"
        (:status response) => status/redirect?))))


