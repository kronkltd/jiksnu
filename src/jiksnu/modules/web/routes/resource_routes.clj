(ns jiksnu.modules.web.routes.resource-routes
  (:require [ciste.loader :refer [require-namespaces]]
            [jiksnu.actions.resource-actions :refer [delete discover index show update]]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers :refer [angular-resource page-resource]]
            [octohipster.mixins :as mixin]))

;; =============================================================================

(defgroup resources
  :url "/main/resources")

(defresource resources collection
  :desc "Collection route for resources"
  :mixins [angular-resource])

(defresource resources resource
  :url "/{_id}"
  :mixins [angular-resource])

;; =============================================================================

(defgroup resources-api
  :url "/api/resources")

(defresource resources-api collection
  :mixins [page-resource]
  :ns 'jiksnu.actions.resource-actions)

;; =============================================================================

(defn routes
  []
  [
   [[:get    "/resources.:format"]   #'index]
   ;; [[:get    "/resources"]   #'index]
   [[:get    "/resources/:id.:format"]     #'show]
   ;; [[:get    "/resources/:id"]     #'show]
   [[:post   "/resources/:id/discover.:format"] #'discover]
   [[:post   "/resources/:id/discover"] #'discover]
   [[:post   "/resources/:id/update.:format"]   #'update]
   [[:post   "/resources/:id/update"]   #'update]
   [[:delete "/resources/:id"]     #'delete]
   [[:post   "/resources/:id/delete"]   #'delete]
   [[:get    "/model/resources/:id"]    #'show]
   ])

(defn pages
  []
  [
   [{:name "resources"}    {:action #'index}]
   ])
