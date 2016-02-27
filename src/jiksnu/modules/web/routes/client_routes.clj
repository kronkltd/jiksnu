(ns jiksnu.modules.web.routes.client-routes
  (:require [ciste.config :refer [config]]
            [clj-time.coerce :as coerce]
            [clojure.string :as string]
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
  :allowed-methods [:get]
  ;; :mixins [mixin/handled-resource]
  :available-media-types ["text/plain"]
  :collection-key :collection
  :respond-with-entity? true
  :new? false
  :can-put-to-missing? false
  ;; :schema {:type "object"
  ;;          :properties {}}
  :handle-ok (fn [ctx]
               (let [params (get-in ctx [:request :params])
                     rt (actions.request-token/get-request-token params)]
                 (->> {:oauth_token (:_id rt)
                       :oauth_token_secret (:secret rt)}
                      (map (fn [[k v]] (str (name k) "=" v)))
                      (string/join "&"))))

  ;; (fn [ctx]
  ;;            {:data (actions.oauth/request-token (util/inspect (:request ctx)))})
  ;; :post! (fn [ctx]
  ;;          (let [params (get-in ctx [:request :params])
  ;;                rt (util/inspect (actions.request-token/get-request-token params))
  ;;                response {:token (:_id rt)
  ;;                          :token_secret (:secret rt)}]
  ;;            {:data response}))

  )
