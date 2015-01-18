(ns jiksnu.modules.web.routes.conversation-routes
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as activity]
            [jiksnu.actions.conversation-actions :as conversation]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [octohipster.mixins :as mixin])
  (:import jiksnu.model.Conversation))

(defgroup conversations
  :url "/main/conversations"
  ;; :resources [conversation-collection]

  )

(defresource conversations collection
  :summary "Index Conversations"
  :desc "collection of conversations"
  :mixins [mixin/collection-resource]
  :available-media-types ["application/json"]
  :data-key :page
  :exists? (fn [ctx]
             {:page (conversation/index)}
             )
  :handle-ok (fn [ctx]
               (json/json-str (:page ctx))))

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
