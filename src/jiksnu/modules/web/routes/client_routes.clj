(ns jiksnu.modules.web.routes.client-routes
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.actions.oauth-actions :as actions.oauth]
            [jiksnu.actions.request-token-actions :as actions.request-token]
            [jiksnu.modules.http.resources
             :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers
             :refer [angular-resource page-resource]]
            [octohipster.mixins :as mixin]))

(defgroup jiksnu client-api
  :name "Client API"
  :url "/api/client")

(defresource client-api :register
  :url "/register"
  :methods {:get {:summary "Register Client"}
            :post {:summary "Register Client"}}
  :allowed-methods [:get :post]
  ;; :mixins [mixin/item-resource]
  :exists? (fn [ctx]
             {:data (log/spy :info (actions.client/register (log/spy :info (:params (:request ctx)))))})
  :post! (fn [ctx]
           true #_{:data (actions.client/register (log/spy :info (:params (:request ctx))))}))

(defgroup jiksnu oauth
  :name "OAuth API"
  :url "/oauth"
  )

(defresource oauth :access-token
  :name "Access Token"
  :url "/access_token"
  :exists? (fn [ctx]
             (actions.oauth/access-token (:request ctx))
             )
  )

(defresource oauth :authorize
  :name "Authorize"
  :url "/authorize"
  :methods {:get {:summary "Authorize Client"
                  :state "authorizeClient"
                  }
            :post {:summary "Do Authorize Client"}
            }
  :exists? (fn [ctx]

             )
  :post! (fn [ctx]
           (actions.oauth/authorize (:request ctx))

           )
  )

(defresource oauth :request-token
  :name "Request Token"
  :url "/request_token"
  :exists? (fn [ctx]
             (actions.oauth/request-token (:request ctx)))
  :post! (fn [ctx]
           (actions.request-token/get-request-token (:request ctx))
           )


  )
