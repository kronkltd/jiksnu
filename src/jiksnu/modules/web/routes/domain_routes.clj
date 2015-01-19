(ns jiksnu.modules.web.routes.domain-routes
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as domain]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers :as helpers :refer [page-resource]]
            [octohipster.mixins :as mixin]
            ))


(defgroup domains
  :name "Domains"
  :url "/main/domains"
  ;; :resources [
  ;;             domain-collection
  ;;             domain-discover
  ;;             domain-resource
  ;;             domain-edit
  ;;             ]
  )

(defresource domains collection
  :desc "collection of domains"
  :mixins [page-resource]
  :index #'domain/index
  :counter (fn [ctx]
             (log/info "counting activities")
             2))

(defresource domains discover
  :url "/{_id}/discover"
  :post domain/discover)

(defresource domains edit
  :url "/{_id}/edit"
  :handle-ok domain/edit-page)

(defresource domains resource
  :url "/{_id}"
  :mixins [mixin/item-resource]
  :delete! domain/delete
  :delete-summary "Delete a domain"
  )

(defgroup well-known
  :url "/.well-known"
  :name "Well Known"
  :summary "Well Known"
  ;; :resources [host-meta]
  )

(defresource well-known host-meta
  :url "/host-meta"
  :summary "Webfinger Host Meta Document"
  :handle-ok domain/host-meta
  )

(defn routes
  []
  [
   ;; [[:get    "/.well-known/host-meta.:format"]   #'domain/show]
   ;; [[:get    "/.well-known/host-meta"]           {:action #'domain/show
   ;;                                                :format :xrd}]
   ;; [[:get    "/main/domains.:format"]            #'domain/index]
   ;; [[:get    "/main/domains"]                    #'domain/index]
   ;; [[:get    "/main/domains/:id.:format"]        #'domain/show]
   ;; [[:get    "/main/domains/:id"]                #'domain/show]
   ;; [[:delete "/main/domains/*"]                  #'domain/delete]
   ;; [[:post   "/main/domains/:id/discover"]       #'domain/discover]
   ;; [[:post   "/main/domains/:id/edit"]           #'domain/edit-page]
   ;; [[:post   "/main/domains"]                    #'domain/find-or-create]
   ;; [[:get    "/api/dialback"]                    #'domain/dialback]
   ])

(defn pages
  []
  [
   [{:name "domains"}    {:action #'domain/index}]
   ])
