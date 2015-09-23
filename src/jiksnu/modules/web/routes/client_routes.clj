(ns jiksnu.modules.web.routes.client-routes
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.client-actions :as actions.client]
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
