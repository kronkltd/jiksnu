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
  :name "Client Models"
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
  :exists? (fn [ctx] {:data (some-> ctx :request :params actions.client/register)})
  ;; :post! (fn [ctx] {:data (some-> ctx :request :params actions.client/register)})
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
  :mixins [angular-resource]
  :methods {:get {:state "authorizeClient"}
            :post {:summary "Do Authorize Client"}}
  :post! (fn [ctx]
           (let [request (util/inspect (:request ctx))
                 params (get-in ctx [:request :params])
                 author (model.user/get-user (:current (friend/identity request)))
                 token-id (get-in ctx [:request :params :oauth_token])
                 rt (model.request-token/fetch-by-id token-id)]
             (util/inspect (actions.request-token/authorize params))
             {:data rt}))
  )

(defresource oauth :request-token
  :name "Request Token"
  :url "/request_token"
  :summary "Get a request token"
  :allowed-methods [:get :post]
  :available-media-types ["text/plain"]
  :respond-with-entity? true
  :exists? (fn [ctx]
             (let [request (:request ctx)
                   client-id (get-in request [:authorization-client :_id])
                   params (-> (:params request)
                              (assoc :client client-id))
                   rt (actions.request-token/get-request-token params)]
               {:data (util/params-encode
                       {:oauth_token (:_id rt)
                        :oauth_token_secret (:secret rt)})}))
  :handle-ok (fn [ctx] (:data ctx))
  :post! (fn [ctx] (:data ctx)))
