(ns jiksnu.modules.web.routes.client-routes
  (:require [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.modules.http.resources
             :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers
             :refer [angular-resource page-resource]]
            [octohipster.mixins :as mixin]))


(defgroup client-api
  :name "Client API"
  :url "/api/client"
)

(defresource client-api register
  :url "/register"
  :id "registerClient"
  :mixins [mixin/item-resource]
  :post! (fn [ctx]
           (actions.client/register (:request ctx))
)
)

(defn routes
  []
  [[[:post "/api/client/register"]           {:action #'actions.client/register
                                              :format :json}]]
  )

(defn pages
  []
  [
   [{:name "clients"} {:action #'actions.client/index}]
   ]
  )
