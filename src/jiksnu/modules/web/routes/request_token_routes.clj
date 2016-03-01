(ns jiksnu.modules.web.routes.request-token-routes
  (:require [jiksnu.actions.request-token-actions :as actions.request-token]
            [jiksnu.model.request-token :as model.request-token]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource
                                                subpage-resource path]]
            [jiksnu.util :as util]
            [liberator.representation :refer [as-response ring-response]]
            [octohipster.mixins :as mixin]
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre]
            [jiksnu.model.user :as model.user]))

(defparameter :model.request-token/id
  :description "The Id of a group membership"
  :type "string")

(defgroup jiksnu request-tokens
  :url "/main/request-tokens"
  :name "Group Memberships")

(defresource request-tokens :collection
  :mixins [angular-resource])

(defresource request-tokens :item
  :url "/{_id}"
  :parameters {:_id (path :model.request-token/id)}
  :mixins [angular-resource])

(defgroup jiksnu request-tokens-api
  :name "Request Token Models"
  :url "/model/request-tokens")

(defresource request-tokens-api :collection
  :mixins [page-resource]
  :allowed-methods [:get]
  :available-formats [:json]
  :ns 'jiksnu.actions.request-token-actions)

(defresource request-tokens-api :item
  :desc "Resource routes for single Request Token"
  :url "/{_id}"
  :parameters {:_id (path :model.request-token/id)}
  :authorized? (fn [ctx]
                 (if (#{:delete} (get-in ctx [:request :request-method]))
                   (when-let [username (get-in ctx [:request :session :cemerick.friend/identity :current])]
                     {:username username})
                   {:username nil}))
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :delete! (fn [ctx]
             (when-let [user (some-> ctx :username model.user/get-user)]
               (if-let [item (:data ctx)]
                 (actions.request-token/delete item)
                 (throw+ "No data"))))
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)]
               {:data (model.request-token/fetch-by-id id)})))
