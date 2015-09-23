(ns jiksnu.modules.web.routes.resource-routes
  (:require [ciste.loader :refer [require-namespaces]]
            [jiksnu.actions.resource-actions :refer [delete discover index show
                                                     update-record]]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource path]]
            [octohipster.mixins :as mixin]))

(defparameter :model.resource/id
  :description "The Id of a conversation"
  :type "string")

;; =============================================================================

(defgroup resources
  :name "Resources"
  :url "/main/resources")

(defresource resources :collection
  :desc "Collection route for resources"
  :mixins [angular-resource])

(defresource resources :item
  :name "Show Resource"
  :url "/{_id}"
  :parameters {:_id (path :model.resource/id)}
  :mixins [angular-resource])

;; =============================================================================

(defgroup resources-api
  :name "Resource API"
  :url "/model/resources")

(defresource resources-api :collection
  :mixins [page-resource]
  :available-formats [:json]
  :ns 'jiksnu.actions.resource-actions)
