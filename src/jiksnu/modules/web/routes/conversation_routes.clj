(ns jiksnu.modules.web.routes.conversation-routes
  (:require [jiksnu.model.conversation :as model.conversation]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter
                                                item-resource page-resource path
                                                subpage-resource]]
            [octohipster.mixins :as mixin]))

(defn get-conversation
  "Gets the item from context by id"
  [ctx]
  (let [id (-> ctx :request :route-params :_id)]
    (model.conversation/fetch-by-id id)))

(defparameter :model.conversation/id
  :description "The Id of a conversation"
  :type "string")

;; =============================================================================

(defgroup jiksnu conversations
  :name "Conversations"
  :url "/main/conversations")

(defresource conversations :collection
  :summary "Index Conversations"
  :desc "collection of conversations"
  :mixins [angular-resource])

(defresource conversations :resource
  :mixins [angular-resource]
  :parameters {:_id (path :model.conversation/id)}
  :url "/{_id}")

;; =============================================================================

(defgroup jiksnu conversations-pump-api
  :name "Pump API - Conversations"
  :url "/api/conversations")

;; =============================================================================

(defgroup jiksnu conversations-api
  :name "Conversation Models"
  :url "/model/conversations")

(defresource conversations-api :collection
  :mixins [page-resource]
  :page "conversations"
  :available-formats [:json]
  :ns 'jiksnu.actions.conversation-actions)

(defresource conversations-api :item
  :desc "Resource routes for single Conversation"
  :url "/{_id}"
  :ns 'jiksnu.actions.conversation-actions
  :parameters {:_id (path :model.conversation/id)}
  :mixins [item-resource])

(defresource conversations-api :activities-stream
  :url "/{_id}/activities"
  :name "Conversation activities"
  :description "Activities related to a conversation"
  :mixins [subpage-resource]
  :target-model "conversation"
  :subpage "activities"
  :parameters {:_id (path :model.conversation/id)})
