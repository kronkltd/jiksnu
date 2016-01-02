(ns jiksnu.modules.web.routes.subscription-routes
  (:require [ciste.core :refer [with-context]]
            [ciste.sections.default :refer [index-section show-section]]
            [clojure.data.json :as json]
            [jiksnu.actions.group-actions :as group]
            [jiksnu.actions.stream-actions :as stream]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as user]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.core.views.stream-views
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource
                                                path subpage-resource]]
            [liberator.core :as lib]
            [octohipster.mixins :as mixin])
  (:import jiksnu.model.Activity
           jiksnu.model.Group
           jiksnu.model.User))

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
  :name "Subscriptions API"
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
