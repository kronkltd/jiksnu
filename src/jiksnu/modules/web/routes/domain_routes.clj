(ns jiksnu.modules.web.routes.domain-routes
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as domain]
            [jiksnu.modules.web.routes :as r]
            [octohipster.core :refer [defresource defgroup]]
            [octohipster.mixins :as mixin]
            ))


(defresource domain-collection
  :desc "collection of domains"
  :handle-ok domain/index
  :count domain/count
  :mixins [mixin/collection-resource])

(defresource domain-discover
  :url "/{_id}/discover"
  :post domain/discover)

(defresource domain-edit
  :url "/{_id}/discover"
  :handle-ok domain/edit)

(defresource domain-resource
  :url "/{_id}"
  :mixins [mixin/item-resource]
  :delete! domain/delete)

(defgroup domains
  :url "/main/domains"
  :resources [
              domain-collection
              domain-discover
              domain-resource
              domain-edit
              ]
  )

(defresource host-meta
  :url "/host-meta"
  :handle-ok domain/host-meta
  )

(defgroup well-known
  :url "/.well-known"
  :resources [host-meta]
  )

(defn on-loaded
  []
  (log/info "adding domain groups")

  (dosync
   (alter r/groups conj domains))

  (dosync
   (alter r/groups conj well-known))

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
