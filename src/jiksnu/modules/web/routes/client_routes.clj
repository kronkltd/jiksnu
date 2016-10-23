(ns jiksnu.modules.web.routes.client-routes
  (:require [cemerick.friend :as friend]
            [jiksnu.actions.access-token-actions :as actions.access-token]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.actions.request-token-actions :as actions.request-token]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.request-token :as model.request-token]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource item-resource
                                                page-resource path]]
            [jiksnu.util :as util]
            [liberator.representation :refer [as-response ring-response]]
            [octohipster.mixins :as mixin]
            [ring.util.codec :as codec]
            [slingshot.slingshot :refer [throw+]]))

(defgroup jiksnu clients
  :name "Clients"
  :url "/main/clients")

(defresource clients :collection
  :summary "Index Clients"
  :desc "collection of clients"
  :mixins [angular-resource])

(defresource clients :resource
  :mixins [angular-resource]
  :parameters {:_id (path :model.client/id)}
  :url "/{_id}")

;; =============================================================================

(defgroup jiksnu clients-api
  :name "Client Models"
  :url "/model/clients")

(defresource clients-api :collection
  :mixins [page-resource]
  :available-formats [:json]
  :ns 'jiksnu.actions.client-actions)

(defresource clients-api :item
  :desc "Resource routes for single Conversation"
  :url "/{_id}"
  :ns 'jiksnu.actions.client-actions
  :parameters {:_id (path :model.client/id)}
  :mixins [item-resource])

;; =============================================================================

(defgroup jiksnu oauth-client-api
  :name "OAuth Client API"
  :url "/api/client")

(defresource oauth-client-api :register
  :url "/register"
  :methods {:get {:summary "Register Client"}
            :post {:summary "Register Client"}}
  :allowed-methods [:get :post]
  :mixins [mixin/handled-resource]
  :available-media-types ["application/json"]
  :collection-key :collection
  :respond-with-entity? true
  :new? false
  :can-put-to-missing? false
  :exists? (fn [ctx] {:data (some-> ctx :request :params actions.client/register)})
  ;; :post! (fn [ctx] {:data (some-> ctx :request :params actions.client/register)})
  :handle-created :data)

;; =============================================================================

(defgroup jiksnu oauth
  :name "OAuth API"
  :url "/oauth")

(defresource oauth :access-token
  :name "Access Token"
  :url "/access_token"
  :allowed-methods [:get :post]
  :available-media-types ["application/json"]
  :new? false
  :respond-with-entity? true
  :post-redirect? false
  :exists? (fn [ctx]
             (let [at (-> ctx :request :authorization-parts
                          actions.access-token/get-access-token)]
               {:data
                (codec/form-encode {:oauth_token (:_id at)
                                    :oauth_token_secret (:secret at)})}))
  :handle-ok (fn [ctx] (:data ctx))
  :post! (fn [ctx] (:data ctx)))

(defresource oauth :authorize
  :name "Authorize"
  :url "/authorize"
  :mixins [angular-resource]
  :methods {:get {:state "authorizeClient"}
            :post {:summary "Do Authorize Client"}}
  :allowed-methods [:get :post]
  :new? false
  :respond-with-entity? true
  :post-redirect? (fn [ctx]
                    (let [rt (::data ctx)]
                      {:location (format "%s?oauth_token=%s&oauth_verifier=%s"
                                         (:callback rt)
                                         (:_id rt) (:verifier rt))}))
  :exists? (fn [ctx]
             (let [request (:request ctx)
                   author (model.user/get-user (:current (friend/identity request)))
                   token-id (get-in ctx [:request :params :oauth_token])
                   rt (model.request-token/fetch-by-id token-id)]
               {::data rt}))
  :post! (fn [ctx]
           ;; TODO: Mark Request Token as used
           (let [rt (::data ctx)]
             {:data {:url rt}})))

(defresource oauth :request-token
  :name "Request Token"
  :url "/request_token"
  :summary "Get a request token"
  :allowed-methods [:get :post]
  :post-redirect? false
  :new? false
  :respond-with-entity? true
  :available-media-types ["text/plain"]
  :exists? (fn [ctx]
             (let [request (:request ctx)
                   client-id (get-in request [:authorization-client :_id])
                   {callback         "oauth_callback"
                    consumer-key     "oauth_consumer_key"
                    nonce            "oauth_nonce"
                    signature        "oauth_signature"
                    signature-method "oauth_signature_method"
                    timestamp        "oauth_timestamp"
                    version          "oauth_version"
                    :as parts} (:authorization-parts request)]
               (if (= version "1.0")
                 ;; TODO: Verify signature
                 (let [params (merge (:params request)
                                     {:client consumer-key
                                      :nonce nonce
                                      :callback (codec/url-decode callback)
                                      :timestamp timestamp})
                       rt (actions.request-token/get-request-token params)]
                   {:data (util/params-encode
                           {:oauth_token (:_id rt)
                            :oauth_token_secret (:secret rt)})})
                 (throw+ {:message "Invalid version"}))))
  :handle-ok (fn [ctx] (:data ctx))
  :post! (fn [ctx] (:data ctx)))
