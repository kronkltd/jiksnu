(ns jiksnu.modules.web.routes.request-token-routes
  (:require [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter item-resource
                                                page-resource subpage-resource path]]
            [liberator.representation :refer [as-response ring-response]]
            [slingshot.slingshot :refer [throw+]]))

(defparameter :model.request-token/id
  :description "The Id of a group membership"
  :type "string")

(defgroup jiksnu request-tokens
  :url "/main/request-tokens"
  :name "Group Memberships")

(defresource request-tokens :collection
  :mixins [angular-resource])

(defresource request-tokens :item
  :url "/{_id}"
  :parameters {:_id (path :model.request-token/id)}
  :mixins [angular-resource])

(defgroup jiksnu request-tokens-api
  :name "Request Token Models"
  :url "/model/request-tokens")

(defresource request-tokens-api :collection
  :mixins [page-resource]
  :page "request-tokens"
  :ns 'jiksnu.actions.request-token-actions)

(defresource request-tokens-api :item
  :desc "Resource routes for single Request Token"
  :url "/{_id}"
  :ns 'jiksnu.actions.request-token-actions
  :parameters {:_id (path :model.request-token/id)}
  :mixins [item-resource])
