(ns jiksnu.modules.web.routes.access-token-routes-test
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.modules.web.middleware :as m]
            [jiksnu.mock :as mock]
            [jiksnu.routes-helper :refer [as-user response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(future-fact "get access token"

  (fact "when given valid params"
    (let [client (mock/a-client-exists)
          request-token (mock/a-request-token-exists {:client client})
          url "/oauth/access_token"
          auth-params {"oauth_signature_method" "HMAC-SHA1"
                       "oauth_consumer_key" (:_id client)
                       "oauth_version" "1.0"
                       "oauth_timestamp" "1380467034"
                       "oauth_nonce" "1800452293"
                       "oauth_verifier" "OLIUZE2KK7DZUGMG3XVP23DUMA"
                       "oauth_token" (:_id request-token)
                       "oauth_signature" "LZITIZS2yXc5zLzL0Mdtjko2oCM%3D"}

          authorization-str (m/authorization-header auth-params)

          response (-> (req/request :post url)
                       (assoc-in [:headers "authorization"] authorization-str)
                       response-for)]

      (fact "should be successful"
        (:status response) => status/success?)
      ))
  )


