(ns jiksnu.modules.web.routes.subscription-routes
  (:require [ciste.core :refer [with-context]]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource
                                                path]]
            [octohipster.mixins :as mixin]))

(defgroup jiksnu subscriptions
  :url "/main/subscriptions"
  :name "Subscriptions"
  :description "Routes related to subscriptions")

(defresource subscriptions :collection
  :name "list subscriptions"
  :desc "Collection route for subscriptions"
  :mixins [angular-resource])

(defresource subscriptions :resource
  :url "/{_id}"
  :name "show subscription"
  :description "show a subscription"
  :parameters {:_id  (path :model.subscription/id)}
  :mixins [angular-resource])

(defgroup jiksnu subscriptions-api
  :name "Subscription Models"
  :url "/model/subscriptions")

(defresource subscriptions-api :collection
  :mixins [page-resource]
  :available-formats [:json]
  :ns 'jiksnu.actions.subscription-actions)

(defresource subscriptions-api :item
  :desc "Resource routes for single Subscription"
  :url "/{_id}"
  :name "subscription routes"
  :mixins [mixin/item-resource]
  :parameters {:_id (path :model.subscription/id)}
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)]
               (when-let [subscription (model.subscription/fetch-by-id id)]
                 {:data subscription}))))

;; (defgroup jiksnu ostatus
;;   :url "/main"
;;   :name "OStatus")

;; (defresource ostatus :sub)
