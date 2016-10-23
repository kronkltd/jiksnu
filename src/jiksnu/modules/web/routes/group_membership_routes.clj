(ns jiksnu.modules.web.routes.group-membership-routes
  (:require [jiksnu.actions.group-membership-actions :as actions.group-membership]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter
                                                item-resource page-resource path]]
            [liberator.representation :refer [as-response ring-response]]
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre]))

(defparameter :model.group-membership/id
  :description "The Id of a group membership"
  :type "string")

(defgroup jiksnu group-memberships
  :url "/main/group-memberships"
  :name "Group Memberships")

(defresource group-memberships :collection
  :mixins [angular-resource])

(defresource group-memberships :item
  :url "/{_id}"
  :parameters {:_id (path :model.group-membership/id)}
  :mixins [angular-resource])

(defgroup jiksnu group-memberships-api
  :name "Group Membership Models"
  :url "/model/group-memberships")

(defn group-membership-api-collection-post!
  [ctx]
  (timbre/info "Post to group membership")
  (let [params (:params (:request ctx))
        group (actions.group-membership/create params)]
    {:data (:_id group)}))

(defresource group-memberships-api :collection
  :mixins [page-resource]
  :new? :data
  :page "group-memberships"
  :post-redirect? (fn [ctx] {:location (format "/model/groups/%s" (:data ctx))})
  :schema {:type "object"
           :properties {:name {:type "string"}}
           :required [:name]}
  :post! group-membership-api-collection-post!
  :ns 'jiksnu.actions.group-membership-actions)

(defresource group-memberships-api :item
  :desc "Resource routes for single Group Membership"
  :url "/{_id}"
  :ns 'jiksnu.actions.group-membership-actions
  :parameters {:_id (path :model.group-membership/id)}
  :mixins [item-resource])
