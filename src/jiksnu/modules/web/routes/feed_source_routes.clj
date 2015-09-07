(ns jiksnu.modules.web.routes.feed-source-routes
  (:require [jiksnu.actions.feed-source-actions :as feed-source]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource path]]
            [octohipster.mixins :as mixin]))

(defparameter :model.feed-source/id
  :description "The feed source id"
  :type "string")

;; =============================================================================

(defgroup feed-sources
  :url "/main/feed-sources")

(defresource feed-sources collection
  :desc "Collection route for feed-sources"
  :mixins [angular-resource])

(defresource feed-sources resource
  :url "/{_id}"
  :parameters {:_id (path :model.feed-source/id)}
  :mixins [angular-resource])

;; =============================================================================

(defgroup feed-sources-api
  :url "/model/feed-sources")

(defresource feed-sources-api collection
  :mixins [page-resource]
  :available-formats [:json]
  :ns 'jiksnu.actions.feed-source-actions)

;; =============================================================================

(defn routes
  []
  [
   [[:get "/main/feed-sources"]  #'feed-source/index]
   [[:get "/main/feed-sources.:format"]  #'feed-source/index]
   [[:get "/main/feed-sources/:id.:format"]  #'feed-source/show]
   [[:get "/main/feed-sources/:id"]  #'feed-source/show]
   [[:get "/main/push/callback"]     #'feed-source/process-updates]
   [[:get "/model/feed-sources/:id"] #'feed-source/show]
   ])

(defn pages
  []
  [
   [{:name "feed-sources"}    {:action #'feed-source/index}]
   ])
