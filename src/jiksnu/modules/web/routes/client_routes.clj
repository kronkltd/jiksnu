(ns jiksnu.modules.web.routes.client-routes
  (:require [ciste.config :refer [config]]
            [clj-time.coerce :as coerce]
            [taoensso.timbre :as timbre]
            [jiksnu.actions.access-token-actions :as actions.access-token]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.actions.oauth-actions :as actions.oauth]
            [jiksnu.actions.request-token-actions :as actions.request-token]
            [jiksnu.model.client :as model.client]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource page-resource path]]
            [jiksnu.util :as util]
            [liberator.representation :refer [ring-response]]
            [octohipster.mixins :as mixin]
            [slingshot.slingshot :refer [throw+ try+]]))

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
  :available-media-types ["application/json"]
  ;; :mixins [mixin/item-resource]
  :exists? (fn [ctx]
             (let [params (:params (:request ctx))]
               {:data true #_(actions.client/register params)}))
  :post! (fn [ctx]
           (let [params (:params (:request ctx))]
             (when-let [item (actions.client/register (util/inspect params))]
               (let [client-id (:_id item)
                     created (int (/ (coerce/to-long (:created item)) 1000))
                     token (:token item)
                     client-uri (format "https://%s/oauth/request_token" (config :domain))
                     secret (:secret item)
                     expires (:secret-expires item)
                     response (merge {:client_id client-id
                                      :client_id_issued_at created
                                      :registration_access_token token
                                      :registration_client_uri client-uri}
                                     (when secret
                                       {:client_secret secret})
                                     (when expires
                                       {:expires_at expires}))]
                 ;; (ring-response
                 ;;  (util/inspect response)
                 ;;  {:status 200})
                 {:data response}
                 ))))
  :handle-created :data)

(defgroup jiksnu oauth
  :name "OAuth API"
  :url "/oauth")

(defresource oauth :access-token
  :name "Access Token"
  :url "/access_token"
  :allowed-methods [:post]
  :available-media-types ["application/json"]
  :exists? (fn [ctx]
             (try+
              (let [params (:authorization-parts (:params (:request ctx)))
                    {:keys [_id secret]} (actions.access-token/get-access-token (util/inspect params))]
                {:body (format "oauth_token=%s&oauth_token_secret=%s" _id secret)})
              (catch Object ex
                (timbre/error ex)
                {:status 500})))
  :post! (fn [ctx]
           (timbre/info "posting")))

(defresource oauth :authorize
  :name "Authorize"
  :url "/authorize"
  :methods {:get {:summary "Authorize Client"
                  :state "authorizeClient"}
            :post {:summary "Do Authorize Client"}}
  :exists? (fn [ctx])
  :post! (fn [ctx] (actions.oauth/authorize (:request ctx))))

(defresource oauth :request-token
  :name "Request Token"
  :url "/request_token"
  :allowed-methods [:post]
  ;; :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :schema {:type "object"
           :properties {}}
  :exists? (fn [ctx]
             {:data (actions.oauth/request-token (:request ctx))})
  :post! (fn [ctx]
           (let [params (get-in ctx [:request :params])
                 rt (util/inspect (actions.request-token/get-request-token params))]
             [200 {:data
                   (util/inspect
                    {:token (:_id rt)
                     :token_secret (:secret rt)})}])))
