(ns jiksnu.modules.web.routes.client-routes-test
  (:require [clj-factory.core :refer [fseq]]
            [clojure.data.json :as json]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.modules.web.middleware :as m]
            jiksnu.modules.web.routes.client-routes
            [jiksnu.routes-helper :refer [json-response response-for]]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "route: client-api/register :post"
  (db/drop-all!)
  (let [params {:type "client_associate"
                :application_type "native"
                :application_name (fseq :word)
                :logo_url (fseq :uri)
                :redirect_uris "oob"
                :registration_access_token (fseq :word)}
        request (-> (req/request :post "/api/client/register")
                    (req/content-type "application/json")
                    (req/body (json/json-str params)))]
    (json-response request) =>
    (contains
     ;; TODO: verify against spec
     {:status  200
      :headers (contains
                {"Content-Type" "application/json"})
      :json    (contains
                {:client_id               string?
                 :registration_client_uri string?
                 :client_id_issued_at     number?})})))

(future-fact "route: oauth/access-token :get"
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
          request (-> (req/request :post url)
                      (assoc-in [:headers "authorization"] authorization-str))]

      (response-for request) => (contains {:status status/success?
                                           :body #(not= "null" %)}))))
