(ns jiksnu.modules.web.routes.domain-routes
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.service-actions :as actions.service]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.modules.http.resources
             :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers
             :refer [angular-resource defparameter page-resource path]]
            [octohipster.mixins :as mixin]))


(defparameter :model.domain/id
  :description "The Id of an domain"
  :type "string")

;; =============================================================================

(defgroup domains
  :name "Domains"
  :url "/main/domains")

(defresource domains collection
  :desc "collection of domains"
  :mixins [angular-resource])

;; (defresource domains discover
;;   :url "/{_id}/discover"
;;   :post actions.service/discover)

;; (defresource domains edit
;;   :url "/{_id}/edit"
;;   :handle-ok actions.domain/edit-page)

(defresource domains resource
  :url "/{_id}"
  :parameters {:_id (path :model.domain/id)}
  :mixins [angular-resource]
  :delete! actions.domain/delete
  :delete-summary "Delete a domain")

;; =============================================================================

(defgroup domains-api
  :name "Domains API"
  :url "/model/domains")

(defresource domains-api collection-api
  :mixins [page-resource]
  :available-formats [:json]
  :ns 'jiksnu.actions.domain-actions)

(defresource domains-api api-item
  :desc "Resource routes for single Domain"
  :url "/{_id}"
  :parameters {:_id (path :model.domain/id)}
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)
                   activity (model.domain/fetch-by-id id)]
               {:data activity}))
  :delete! #'actions.domain/delete
  ;; :put!    #'actions.domain/update-record
  )

;; =============================================================================

(defgroup well-known
  :url "/.well-known"
  :name "Well Known"
  :summary "Well Known")

(defresource well-known host-meta
  :url "/host-meta"
  :summary "Webfinger Host Meta Document"
  :available-media-types ["application/json"]
  :handle-ok actions.domain/host-meta)





(defn routes
  []
  [
   ;; [[:get    "/.well-known/host-meta.:format"]   #'actions.domain/show]
   ;; [[:get    "/.well-known/host-meta"]           {:action #'actions.domain/show
   ;;                                                :format :xrd}]
   ;; [[:get    "/main/domains.:format"]            #'actions.domain/index]
   ;; [[:get    "/main/domains"]                    #'actions.domain/index]
   ;; [[:get    "/main/domains/:id.:format"]        #'actions.domain/show]
   ;; [[:get    "/main/domains/:id"]                #'actions.domain/show]
   ;; [[:delete "/main/domains/*"]                  #'actions.domain/delete]
   ;; [[:post   "/main/domains/:id/edit"]           #'actions.domain/edit-page]
   ;; [[:post   "/main/domains"]                    #'actions.domain/find-or-create]
   ;; [[:get    "/api/dialback"]                    #'actions.domain/dialback]
   ])

(defn pages
  []
  [
   [{:name "domains"}    {:action #'actions.domain/index}]
   ])
