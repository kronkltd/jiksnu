(ns jiksnu.modules.web.routes.conversation-routes
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as activity]
            [jiksnu.actions.conversation-actions :as conversation]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers :as helpers
             :refer [angular-resource page-resource]]
            [octohipster.mixins :as mixin])
  (:import jiksnu.model.Conversation))

(defgroup conversations
  :url "/main/conversations")

(defresource conversations resource
  :mixins [angular-resource]
  :url "/{_id}")

(defresource conversations collection
  :summary "Index Conversations"
  :desc "collection of conversations"
  :mixins [angular-resource]
  :ns 'jiksnu.actions.conversation-actions)



(defgroup conversations-api
  :url "/api/conversations")

(defresource conversations-api collection
  :mixins [page-resource]
  :ns 'jiksnu.actions.conversation-actions)



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
