(ns jiksnu.modules.web.routes.service-routes
  (:require [taoensso.timbre :as timbre]
            [jiksnu.actions.service-actions :as actions.service]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter
                                                item-resource page-resource path]]))

(defparameter :model.service/id
  :description "The Id of an service"
  :type "string")

;; =============================================================================

(defgroup jiksnu services
  :name "Services"
  :url "/main/services")

(defresource services :collection
  :desc "collection of services"
  :summary "Index Services"
  :mixins [angular-resource]
  :methods {:get {:state "indexServices"}})

;; (defresource services :discover
;;   :url "/{_id}/discover"
;;   :post actions.service/discover)

;; (defresource services :edit
;;   :url "/{_id}/edit"
;;   :handle-ok actions.service/edit-page)

(defresource services :item
  :summary "Show Service"
  :url "/{_id}"
  :parameters {:_id (path :model.service/id)}
  :mixins [angular-resource]
  :methods {:get {:state "showService"}}
  :delete! actions.service/delete
  :delete-summary "Delete a service")

;; =============================================================================

(defgroup jiksnu services-api
  :name "Service Models"
  :url "/model/services")

(defn services-api-post
  [ctx]
  (timbre/info "Post to Services")
  (let [params (:params (:request ctx))
        item (actions.service/create params)]
    {:data (:_id item)}))

(defn services-allowed?
  [ctx]
  (let [method (get-in ctx [:request :request-method])]
    (if (#{:delete} method)
      ;; TODO: Only administrators can delete this object
      false
      true)))

(defresource services-api :collection
  :mixins [page-resource]
  :available-formats [:json]
  :page "services"
  :new? :data
  :post-redirect? (fn [ctx] {:location (format "/model/services/%s" (:data ctx))})
  :post! services-api-post
  :ns 'jiksnu.actions.service-actions)

(defresource services-api :item
  :desc "Resource routes for single Service"
  :url "/{_id}"
  :ns 'jiksnu.actions.service-actions
  :allowed? services-allowed?
  :parameters {:_id (path :model.service/id)}
  :mixins [item-resource])
