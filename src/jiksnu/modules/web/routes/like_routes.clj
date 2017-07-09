(ns jiksnu.modules.web.routes.like-routes
  (:require [ciste.config :refer [config]]
            [jiksnu.modules.http.resources :refer [add-group! defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter item-resource page-resource path]]))

(defparameter :model.like/id
  :description "The Id of a Like"
  :type "string")

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
  :name "Like Models"
  :url "/model/likes")

(defresource likes-api :collection
  :desc "Collection route for likes"
  :mixins [page-resource]
  :page "likes"
  :methods {:get {:summary "Index Likes"}
            :post {:summary "Create Like"}}
  ;; :post! likes-api-post
  ;; :schema like-schema
  :ns 'jiksnu.modules.core.actions.like-actions)

(defresource likes-api :item
  :desc "Resource routes for single Like"
  :url "/{_id}"
  :ns 'jiksnu.modules.core.actions.like-actions
  :parameters {:_id (path :model.like/id)}
  :methods {:get {:summary "Show Like"}
            :delete {:summary "Delete Like"}}
  :mixins [item-resource])
