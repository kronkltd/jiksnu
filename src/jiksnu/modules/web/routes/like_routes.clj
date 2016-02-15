(ns jiksnu.modules.web.routes.like-routes
  (:require [ciste.config :refer [config]]
            [jiksnu.actions.like-actions :as actions.like]
            [jiksnu.model.like :as model.like]
            [jiksnu.modules.http.resources :refer [add-group! defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource path]]
            [octohipster.mixins :as mixin]))

(defgroup jiksnu likes
  :name "Likes"
  :url "/main/likes")

(defresource likes :collection
  :methods {:get {:summary "Index Likes Page"}}
  :mixins [angular-resource])

(defresource likes :resource
  :url "/{_id}"
  :methods {:get {:summary "Show like Page"}}
  :parameters {:_id (path :model.like/id)}
  :mixins [angular-resource])

;; =============================================================================

(defgroup jiksnu likes-api
  :name "Likes API"
  :url "/model/likes")

(defresource likes-api :collection
  :desc "Collection route for likes"
  :mixins [page-resource]
  :available-formats [:json]
  :allowed-methods [:get :post]
  :available-media-types ["application/json"]
  :methods {:get {:summary "Index Likes"}
            :post {:summary "Create Like"}}
  ;; :post! likes-api-post
  ;; :schema like-schema
  :ns 'jiksnu.actions.like-actions)

(defresource likes-api :item
  :desc "Resource routes for single Like"
  :url "/{_id}"
  :parameters {:_id (path :model.like/id)}
  :methods {:get {:summary "Show Like"}
            :delete {:summary "Delete Like"}}
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)
                   like (model.like/fetch-by-id id)]
               {:data like}))
  ;; :put!    #'actions.like/update-record
  :delete! (fn [ctx] (actions.like/delete (:data ctx))))
