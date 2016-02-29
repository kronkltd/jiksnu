(ns jiksnu.modules.web.routes.group-routes
  (:require [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.model.group :as model.group]
            jiksnu.modules.core.views.group-views
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource
                                                subpage-resource path]]
            [liberator.representation :refer [as-response ring-response]]
            [octohipster.mixins :as mixin]
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre]
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
  :name "Group Models"
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
                 (actions.group/delete item)
                 (throw+ "No data"))))
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)]
               {:data (model.group/fetch-by-id id)})))

(defresource groups-api :members
  :url "/{_id}/members"
  :name "group members"
  :description "Members of {{group}}"
  :mixins [subpage-resource]
  :target-model "group"
  :subpage "members"
  :parameters {:_id (path :model.group/id)}
  :available-formats [:json])
