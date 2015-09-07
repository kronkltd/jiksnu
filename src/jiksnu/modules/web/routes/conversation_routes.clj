(ns jiksnu.modules.web.routes.conversation-routes
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as activity]
            [jiksnu.actions.conversation-actions :as conversation]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource path]]
            [octohipster.mixins :as mixin])
  (:import jiksnu.model.Conversation))

(defparameter :model.conversation/id
  :description "The Id of a conversation"
  :type "string")

;; =============================================================================

(defgroup conversations
  :url "/main/conversations")

(defresource conversations collection
  :summary "Index Conversations"
  :desc "collection of conversations"
  :mixins [angular-resource])

(defresource conversations resource
  :mixins [angular-resource]
  :parameters {:_id (path :model.conversation/id)}
  :url "/{_id}")

;; =============================================================================

(defgroup conversations-pump-api
  :url "/api/conversations"
)

;; =============================================================================

(defgroup conversations-api
  :url "/model/conversations")

(defresource conversations-api collection
  :mixins [page-resource]
  :available-formats [:json]
  :ns 'jiksnu.actions.conversation-actions)

;; =============================================================================

(defn routes
  []
  [[[:get "/main/conversations.:format"] #'conversation/index]
   [[:get "/main/conversations"] #'conversation/index]
   [[:get "/main/conversations/:id.:format"]  #'conversation/show]
   [[:get "/main/conversations/:id"]  #'conversation/show]
   [[:get "/model/conversations/:id"] #'conversation/show]
   ])

(defn pages
  []
  [
   [{:name "conversations"}    {:action #'conversation/index}]
   ])

(defn sub-pages
  []
  [
   [{:type Conversation :name "activities"} {:action #'activity/fetch-by-conversation}]
   ])
