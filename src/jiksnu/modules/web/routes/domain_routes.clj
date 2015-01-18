(ns jiksnu.modules.web.routes.domain-routes
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as domain]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [octohipster.mixins :as mixin]
            ))


(defresource domains collection
  :desc "collection of domains"
  :handle-ok domain/index
  :count domain/count
  :mixins [mixin/collection-resource])

(defresource domains discover
  :url "/{_id}/discover"
  :post domain/discover)

(defresource domains edit
  :url "/{_id}/edit"
  :handle-ok domain/edit-page)

(defresource domains resource
  :url "/{_id}"
  :mixins [mixin/item-resource]
  :delete! domain/delete)

(defgroup domains
  :url "/main/domains"
  ;; :resources [
  ;;             domain-collection
  ;;             domain-discover
  ;;             domain-resource
  ;;             domain-edit
  ;;             ]
  )

(defresource well-known host-meta
  :url "/host-meta"
  :summary "Webfinger Host Meta Document"
  :handle-ok domain/host-meta
  )

(defgroup well-known
  :url "/.well-known"
  ;; :resources [host-meta]
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
