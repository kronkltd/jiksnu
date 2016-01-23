(ns jiksnu.modules.web.routes.client-routes
  (:require [taoensso.timbre :as timbre]
            [jiksnu.actions.access-token-actions :as actions.access-token]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.actions.oauth-actions :as actions.oauth]
            [jiksnu.actions.request-token-actions :as actions.request-token]
            [jiksnu.modules.http.resources
             :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers
             :refer [angular-resource page-resource]]
            [slingshot.slingshot :refer [throw+ try+]]))

(defgroup jiksnu client-api
  :name "Client API"
  :url "/api/client")

(defresource client-api :register
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
             (when-let [client (actions.client/register params)]
               {:data "{status: 'ok'}"})))
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
                    {:keys [_id secret]} (actions.access-token/get-access-token params)]
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
  :exists? (fn [ctx]
             (actions.oauth/request-token (:request ctx)))
  :post! (fn [ctx] (actions.request-token/get-request-token (:request ctx))))
