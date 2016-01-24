(ns jiksnu.modules.web.routes.group-routes
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.model.group :as model.group]
            jiksnu.modules.core.views.group-views
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource path]]
            [liberator.representation :refer [as-response ring-response]]
            [octohipster.mixins :as mixin]
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre]
            [jiksnu.util :as util]
            [jiksnu.model.user :as model.user]))

(defparameter :model.group/id
  :description "The Id of a conversation"
  :type "string")

;; =============================================================================

(defgroup jiksnu groups
  :url "/main/groups"
  :name "Groups")

(defresource groups :collection
  :mixins [angular-resource])

(defresource groups :item
  :url "/{_id}"
  :parameters {:_id (path :model.group/id)}
  :mixins [angular-resource])

;; (defresource groups resource

;;   )

;; =============================================================================

(defgroup jiksnu groups-api
  :name "Groups API"
  :url "/model/groups")

(defresource groups-api :collection
  :mixins [page-resource]
  :allowed-methods [:get :post]
  :new? :data
  :post-redirect? (fn [ctx] {:location (format "/model/groups/%s" (:data ctx))})
  :schema {:type "object"
           :properties {:name {:type "string"}}
           :required [:name]}
  :post! (fn [ctx]
           (timbre/info "Post to group")
           (let [params (:params (:request ctx))
                 group (actions.group/create params)]
             {:data (:_id group)}))
  :available-formats [:json]
  :ns 'jiksnu.actions.group-actions)

(defresource groups-api :item
  :desc "Resource routes for single Group"
  :url "/{_id}"
  :parameters {:_id (path :model.group/id)}
  :authorized? (fn [ctx]
                 (let [method (get-in ctx [:request :request-method])]
                   (if (#{:delete} method)
                     (if-let [username (get-in ctx [:request :session :cemerick.friend/identity :current])]
                       (do
                         (util/inspect username)
                         {:username username})
                       (do
                         (timbre/warn "not authorized")
                         false))
                     (do
                       (timbre/debug "unauthenticated method")
                       true))))
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :delete! (fn [ctx]
             #_(util/inspect ctx)
             (if-let [user (some-> ctx :username model.user/get-user)]
               (do
                 (if-let [item (:data ctx)]
                   (actions.group/delete item)
                   (throw+ "No data")))
               (do
                 (timbre/warn "No auth")
                 (ring-response (as-response {:data "No auth"} ctx) {:status 401})
                 nil)))

             ;#'actions.group/delete
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)]
               {:data (model.group/fetch-by-id id)}))

             )
