(ns jiksnu.modules.web.routes.notification-routes
  (:require [ciste.config :refer [config]]
            [jiksnu.modules.http.resources :refer [add-group! defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter item-resource page-resource path]]))

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
  :page "notifications"
  :methods {:get {:summary "Index Notifications"}
            :post {:summary "Create Notification"}}
  :ns 'jiksnu.actions.notification-actions)

(defresource notifications-api :item
  :desc "Resource routes for single Notification"
  :url "/{_id}"
  :ns 'jiksnu.actions.notification-actions
  :parameters {:_id (path :model.notification/id)}
  :methods {:get {:summary "Show Notification"}
            :delete {:summary "Delete Notification"}}
  :mixins [item-resource])
