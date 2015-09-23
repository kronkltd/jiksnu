(ns jiksnu.modules.web.routes.feed-source-routes
  (:require [jiksnu.actions.feed-source-actions :as feed-source]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource path]]
            [octohipster.mixins :as mixin]))

(defparameter :model.feed-source/id
  :description "The feed source id"
  :type "string")

;; =============================================================================

(defgroup jiksnu feed-sources
  :name "Feed Sources"
  :url "/main/feed-sources")

(defresource feed-sources :collection
  :desc "Collection route for feed-sources"
  :mixins [angular-resource])

(defresource feed-sources :item
  :url "/{_id}"
  :parameters {:_id (path :model.feed-source/id)}
  :mixins [angular-resource])

;; =============================================================================

(defgroup jiksnu feed-sources-api
  :name "Feed Sources API"
  :url "/model/feed-sources")

(defresource feed-sources-api :collection
  :mixins [page-resource]
  :available-formats [:json]
  :ns 'jiksnu.actions.feed-source-actions)
