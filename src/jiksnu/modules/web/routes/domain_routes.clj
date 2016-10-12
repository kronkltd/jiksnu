(ns jiksnu.modules.web.routes.domain-routes
  (:require [taoensso.timbre :as timbre]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.webfinger-actions :as actions.webfinger]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter
                                                item-resource page-resource path]]
            [octohipster.mixins :as mixin]))

(defparameter :model.domain/id
  :description "The Id of an domain"
  :type "string")

;; =============================================================================

(defgroup jiksnu domains
  :name "Domains"
  :url "/main/domains")

(defresource domains :collection
  :desc "collection of domains"
  :summary "Index Domains"
  :mixins [angular-resource]
  :methods {:get {:state "indexDomains"}})

;; (defresource domains :discover
;;   :url "/{_id}/discover"
;;   :post actions.service/discover)

;; (defresource domains :edit
;;   :url "/{_id}/edit"
;;   :handle-ok actions.domain/edit-page)

(defresource domains :item
  :summary "Show Domain"
  :url "/{_id}"
  :parameters {:_id (path :model.domain/id)}
  :mixins [angular-resource]
  :methods {:get {:state "showDomain"}}
  :delete! actions.domain/delete
  :delete-summary "Delete a domain")

;; =============================================================================

(defgroup jiksnu domains-api
  :name "Domain Models"
  :url "/model/domains")

(defresource domains-api :collection
  :mixins [page-resource]
  :available-formats [:json]
  :ns 'jiksnu.actions.domain-actions)

(defresource domains-api :item
  :desc "Resource routes for single Domain"
  :url "/{_id}"
  :ns 'jiksnu.actions.domain-actions
  :parameters {:_id (path :model.domain/id)}
  :mixins [item-resource])

;; =============================================================================

(defgroup jiksnu well-known
  :url "/.well-known"
  :name "Well Known"
  :summary "Well Known")

(defresource well-known :host-meta
  :url "/host-meta"
  :summary "Webfinger Host Meta Document"
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :handle-ok (fn [ctx]
               (timbre/info "host meta")
               (actions.webfinger/host-meta (actions.domain/current-domain))))
