(ns jiksnu.modules.web.routes.client-routes
  (:require [cemerick.friend :as friend]
            [ciste.config :refer [config]]
            [clj-time.coerce :as coerce]
            [clojure.string :as string]
            [taoensso.timbre :as timbre]
            [jiksnu.actions.access-token-actions :as actions.access-token]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.actions.oauth-actions :as actions.oauth]
            [jiksnu.actions.request-token-actions :as actions.request-token]
            [jiksnu.model.client :as model.client]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.request-token :as model.request-token]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource page-resource path]]
            [jiksnu.util :as util]
            [liberator.representation :refer [as-response ring-response]]
            [octohipster.mixins :as mixin]
            [slingshot.slingshot :refer [throw+ try+]]
            [taoensso.timbre :as timbre]))

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
  :name "Clients API"
  :url "/model/clients")

(defresource clients-api :collection
  :mixins [page-resource]
  :available-formats [:json]
  :ns 'jiksnu.actions.client-actions)

(defresource clients-api :item
  :desc "Resource routes for single Conversation"
  :url "/{_id}"
  :parameters {:_id (path :model.client/id)}
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)
                   conversation (model.client/fetch-by-id id)]
               {:data conversation})))

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
  :exists? (fn [ctx]
             (let [params (:params (:request ctx))]
               {:data true #_(actions.client/register params)}))
  :post! (fn [ctx]
           (let [params (:params (:request ctx))]
             (when-let [item (actions.client/register params)]
               (let [{client-id :_id
                      expires :secret-expires
                      :keys [token secret created]} item
                     created (int (/ (coerce/to-long created) 1000))
                     client-uri (format "https://%s/oauth/request_token" (config :domain))
                     response (merge {:client_id client-id
                                      :client_id_issued_at created
                                      :registration_access_token token
                                      :registration_client_uri client-uri}
                                     (when secret
                                       {:client_secret secret})
                                     (when expires
                                       {:expires_at expires}))]
                 {:data response}))))
  :handle-created :data)

(defgroup jiksnu oauth
  :name "OAuth API"
  :url "/oauth")

(defresource oauth :access-token
  :name "Access Token"
  :url "/access_token"
  :allowed-methods [:get :post]
  :available-media-types ["application/json"]
  :mixins [mixin/item-resource]
  :exists? (fn [ctx]
             (try+
              (let [{client :authorization-client
                     params :authorization-parts
                     :as request} (:request ctx)
                    {:keys [_id secret]} (actions.access-token/get-access-token params)]
                {:data (util/params-encode
                        {:oauth_token _id
                         :oauth_token_secret secret})})
              (catch Object ex
                (timbre/error ex)
                {:status 500})))
  :post! (fn [ctx]
           (timbre/info "posting")))

(defresource oauth :authorize
  :name "Authorize"
  :url "/authorize"
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :mixins [mixin/item-resource]
  :methods {:get {:summary "Authorize Client"
                  :state "authorizeClient"}
            :post {:summary "Do Authorize Client"}}
  :exists? (fn [ctx]
             (let [request (util/inspect (:request ctx))
                   params (get-in ctx [:request :params])
                   author (model.user/get-user (:current (friend/identity request)))
                   token-id (get-in ctx [:request :params :oauth_token])
                   rt (model.request-token/fetch-by-id token-id)]
               (util/inspect (actions.request-token/authorize params))
               {:data rt})))

(defresource oauth :request-token
  :name "Request Token"
  :url "/request_token"
  :allowed-methods [:get]
  :available-media-types ["text/plain"]
  :handle-ok (fn [ctx]
               (let [request (:request ctx)
                     client-id (get-in request [:authorization-client :_id])
                     params (-> (:params request)
                                (assoc :client client-id))
                     rt (actions.request-token/get-request-token params)]
                 (util/params-encode
                  {:oauth_token (:_id rt)
                   :oauth_token_secret (:secret rt)}))))
