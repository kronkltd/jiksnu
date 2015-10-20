(ns jiksnu.modules.web.routes.conversation-routes
  (:require [clojure.data.json :as json]
            [taoensso.timbre :as log]
            [jiksnu.actions.activity-actions :as activity]
            [jiksnu.actions.conversation-actions :as conversation]
            jiksnu.modules.core.filters.activity-filters
            jiksnu.modules.core.views.activity-views
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource path subpage-resource]]
            [octohipster.mixins :as mixin])
  (:import jiksnu.model.Conversation))

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
  :name "Conversations API"
  :url "/model/conversations")

(defresource conversations-api :collection
  :mixins [page-resource]
  :available-formats [:json]
  :ns 'jiksnu.actions.conversation-actions)

(defresource conversations-api :item
  :desc "Resource routes for single Conversation"
  :url "/{_id}"
  :parameters {:_id (path :model.conversation/id)}
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)
                   conversation (model.conversation/fetch-by-id id)]
               {:data conversation}))
  ;; :delete! #'actions.conversation/delete
  ;; :put!    #'actions.conversation/update-record
  )

(defresource conversations-api :activities-stream
  :desc "Activities related to a conversation"
  :url "/{_id}/activities"
  :parameters {:_id (path :model.conversation/id)}
  :mixins [subpage-resource]
  :target get-conversation
  :target-model "conversation"
  :subpage "activities"
  :allowed-methods [:get]
  :available-formats [:json]
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)
                   conversation (model.conversation/fetch-by-id id)]
               {:data conversation}))
  ;; :delete! #'actions.conversation/delete
  ;; :put!    #'actions.conversation/update-record
  )
