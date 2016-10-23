(ns jiksnu.modules.web.routes.client-routes-test
  (:require [clj-factory.core :refer [fseq]]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model.access-token :as model.access-token]
            [jiksnu.model.request-token :as model.request-token]
            [jiksnu.modules.web.middleware :as m]
            jiksnu.modules.web.routes.client-routes
            [jiksnu.routes-helper :refer [json-response response-for]]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]
            [ring.util.codec :as codec])
  (:import (org.apache.http HttpStatus)))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(fact "route: client-api/register :post"
  ;; (db/drop-all!)
  (let [params {:type "client_associate"
                :application_type "native"
                :application_name (fseq :word)
                :logo_url (fseq :uri)
                :redirect_uris "oob"
                :registration_access_token (fseq :word)}
        request (-> (req/request :post "/api/client/register")
                    (req/content-type "application/json")
                    (req/body (json/write-str params)))]
    (json-response request) =>
    (contains
     ;; TODO: verify against spec
     {:status  HttpStatus/SC_OK
      :headers (contains
                {"Content-Type" "application/json"})
      :json    (contains
                {:client_id               string?
                 :registration_client_uri string?
                 :client_id_issued_at     number?})})))

(fact "route: client-api/request-token :get"
  (db/drop-all!)
  (let [client (mock/a-client-exists)
        consumer-key ""
        auth-params {:oauth_callback         "oob"
                     :oauth_consumer_key     consumer-key
                     :oauth_nonce            "fd0fa162e56e82515dde75b6863a5d4a"
                     :oauth_signature        "Qbq0HZ%2FqkbFk1jBv0HSsUpTjwKk%3D"
                     :oauth_signature_method "HMAC-SHA1"
                     :oauth_timestamp        1456723873
                     :oauth_version          "1.0"}
        path "/oauth/request_token"
        authorization-str (m/authorization-header auth-params)
        request (-> (req/request :get path)
                    (assoc-in [:headers "authorization"] authorization-str))
        response (-> (response-for request)
                     (update :body (fn [body]
                                     (->> (string/split body #"&")
                                          (map #(string/split % #"="))
                                          (into {})))))]
    response => (contains {:status HttpStatus/SC_OK})
    (let [{{secret "oauth_token_secret" token-id "oauth_token"} :body} response]
      (model.request-token/fetch-by-id token-id) =>
      (contains {:secret secret}))))

(fact "route: client-api/request-token :post"
  (db/drop-all!)
  (let [client (mock/a-client-exists)
        consumer-key ""
        auth-params {:oauth_callback         "oob"
                     :oauth_consumer_key     consumer-key
                     :oauth_nonce            "fd0fa162e56e82515dde75b6863a5d4a"
                     :oauth_signature        "Qbq0HZ%2FqkbFk1jBv0HSsUpTjwKk%3D"
                     :oauth_signature_method "HMAC-SHA1"
                     :oauth_timestamp        1456723873
                     :oauth_version          "1.0"}
        path "/oauth/request_token"
        authorization-str (m/authorization-header auth-params)
        request (-> (req/request :post path)
                    (assoc-in [:headers "authorization"] authorization-str))
        response (-> (response-for request)
                     (update :body codec/form-decode))]
    response => (contains {:status HttpStatus/SC_OK})
    (let [{{secret "oauth_token_secret" token-id "oauth_token"} :body} response]
      (model.request-token/fetch-by-id token-id) =>
      (contains {:secret secret}))))

(fact "route: oauth/access-token :get"
  (fact "when given valid params"
    (let [client (mock/a-client-exists)
          request-token (mock/a-request-token-exists {:client client})
          url "/oauth/access_token"
          auth-params {"oauth_consumer_key" (:_id client)
                       "oauth_token" (:_id request-token)
                       "oauth_signature_method" "HMAC-SHA1"
                       "oauth_signature" "LZITIZS2yXc5zLzL0Mdtjko2oCM%3D"
                       "oauth_timestamp" "1380467034"
                       "oauth_nonce" "1800452293"
                       "oauth_version" "1.0"
                       "oauth_verifier" "OLIUZE2KK7DZUGMG3XVP23DUMA"}

          authorization-str (m/authorization-header auth-params)
          request (-> (req/request :get url)
                      (assoc-in [:headers "authorization"] authorization-str))
          response (-> (response-for request)
                       (update :body codec/form-decode))]

      response => (contains {:status HttpStatus/SC_OK})

      (let [id (get-in response [:body "oauth_token"])
            secret (get-in response [:body "oauth_token_secret"])]
        (model.access-token/fetch-by-id id) => (contains {:secret secret})))))

(fact "route: oauth/access-token :post"
  (fact "when given valid params"
    (let [client (mock/a-client-exists)
          request-token (mock/a-request-token-exists {:client client})
          url "/oauth/access_token"
          auth-params {"oauth_consumer_key" (:_id client)
                       "oauth_token" (:_id request-token)
                       "oauth_signature_method" "HMAC-SHA1"
                       "oauth_signature" "LZITIZS2yXc5zLzL0Mdtjko2oCM%3D"
                       "oauth_timestamp" "1380467034"
                       "oauth_nonce" "1800452293"
                       "oauth_version" "1.0"
                       "oauth_verifier" "OLIUZE2KK7DZUGMG3XVP23DUMA"}
          authorization-str (m/authorization-header auth-params)
          request (-> (req/request :post url)
                      (assoc-in [:headers "authorization"] authorization-str))
          response (-> (response-for request)
                       (update :body codec/form-decode))]

      response => (contains {:status HttpStatus/SC_OK})

      (let [id (get-in response [:body "oauth_token"])
            secret (get-in response [:body "oauth_token_secret"])]
        (model.access-token/fetch-by-id id) => (contains {:secret secret})))))

(future-fact "route: oauth/authorize :get"
  (fact "when authenticated"
    (let [actor (mock/a-user-exists)]

      (fact "when given a valid request token"
        (let [request-token (mock/a-request-token-exists)
              url (format "/oauth/authorize?oauth_token=%s" (:_id request-token))]
          (let [response (-> (req/request :get url)
                             (as-user actor)
                             response-for)]
            (:status response) => HttpStatus/SC_OK))))))
