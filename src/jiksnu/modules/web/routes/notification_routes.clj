(ns jiksnu.modules.web.routes.notification-routes
  (:require [ciste.config :refer [config]]
            [jiksnu.actions.notification-actions :as actions.notification]
            [jiksnu.model.notification :as model.notification]
            [jiksnu.modules.http.resources :refer [add-group! defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource path]]
            [octohipster.mixins :as mixin]))

(defgroup jiksnu notifications
  :name "Notifications"
  :url "/main/notifications")

(defresource notifications :collection
  :methods {:get {:summary "Index Notifications Page"}}
  :mixins [angular-resource])

(defresource notifications :resource
  :url "/{_id}"
  :methods {:get {:summary "Show notification Page"}}
  :parameters {:_id (path :model.notification/id)}
  :mixins [angular-resource])

;; =============================================================================

(defgroup jiksnu notifications-api
  :name "Notification Models"
  :url "/model/notifications")

(defresource notifications-api :collection
  :desc "Collection route for notifications"
  :mixins [page-resource]
  :available-formats [:json]
  :allowed-methods [:get :post]
  :available-media-types ["application/json"]
  :methods {:get {:summary "Index Notifications"}
            :post {:summary "Create Notification"}}
  ;; :post! notifications-api-post
  ;; :schema notification-schema
  :ns 'jiksnu.actions.notification-actions)

(defresource notifications-api :item
  :desc "Resource routes for single Notification"
  :url "/{_id}"
  :parameters {:_id (path :model.notification/id)}
  :methods {:get {:summary "Show Notification"}
            :delete {:summary "Delete Notification"}}
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)
                   notification (model.notification/fetch-by-id id)]
               {:data notification}))
  ;; :put!    #'actions.notification/update-record
  :delete! (fn [ctx] (actions.notification/delete (:data ctx))))
