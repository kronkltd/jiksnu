(ns jiksnu.modules.web.routes.feed-source-routes
  (:require [jiksnu.actions.feed-source-actions :as feed-source]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers :refer [angular-resource page-resource]]
            [octohipster.mixins :as mixin]))

;; =============================================================================

(defgroup feed-sources
  :url "/main/feed-sources")

(defresource feed-sources collection
  :desc "Collection route for feed-sources"
  :mixins [angular-resource])

(defresource feed-sources resource
  :url "/{_id}"
  :mixins [angular-resource])

;; =============================================================================

(defgroup feed-sources-api
  :url "/api/feed-sources")

(defresource feed-sources-api collection
  :mixins [page-resource]
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
