(ns jiksnu.modules.web.routes.group-membership-routes
  (:require [jiksnu.actions.group-membership-actions :as actions.group-membership]
            [jiksnu.model.group-membership :as model.group-membership]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource
                                                subpage-resource path]]
            [liberator.representation :refer [as-response ring-response]]
            [octohipster.mixins :as mixin]
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre]
            [jiksnu.model.user :as model.user]))

(timbre/info "loading")

(defparameter :model.group-membership/id
  :description "The Id of a group membership"
  :type "string")

(defgroup jiksnu group-memberships-api
  :name "Group Memberships API"
  :url "/model/groups-memberships")

(defresource group-memberships-api :collection
  :mixins [page-resource]
  :allowed-methods [:get :post]
  :new? :data
  :post-redirect? (fn [ctx] {:location (format "/model/groups/%s" (:data ctx))})
  :schema {:type "object"
           :properties {:name {:type "string"}}
           :required [:name]}
  :post! (fn [ctx]
           (timbre/info "Post to group membership")
           (let [params (:params (:request ctx))
                 group (actions.group-membership/create params)]
             {:data (:_id group)}))
  :available-formats [:json]
  :ns 'jiksnu.actions.group-membership-actions)

(defresource group-memberships-api :item
  :desc "Resource routes for single Group Membership"
  :url "/{_id}"
  :parameters {:_id (path :model.group-membership/id)}
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
                 (actions.group-membership/delete item)
                 (throw+ "No data"))))
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)]
               {:data (model.group-membership/fetch-by-id id)})))
