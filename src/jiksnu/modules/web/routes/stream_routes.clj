(ns jiksnu.modules.web.routes.stream-routes
  (:require [ciste.commands :refer [add-command!]]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.model.stream :as model.stream]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource path]]
            [octohipster.mixins :as mixin]
))

(defparameter :model.stream/id
  :in :path
  :description "The Id of an stream"
  :type "string")

(defgroup streams
  :name "Streams"
  :url "/main/streams")

(defresource streams :collection
  :mixins [angular-resource])

(defresource streams :item
  :url "/{_id}"
  :parameters {:_id (path :model.stream/id)}
  :mixins [angular-resource])

;; =============================================================================

(defgroup streams-api
  :name "Streams API"
  :url "/model/streams")

(defresource streams-api :collection
  :desc "Collection route for streams"
  :mixins [page-resource]
  :available-formats [:json]
  :allowed-methods [:get :post]
  :post! (fn [ctx]
           #_(let [{{params :params
                   :as request} :request} ctx
                   username (:current (friend/identity request))
                   id (str "acct:" username "@" (config :domain))
                   params (assoc params :author id)]
             (actions.stream/post params)))
  ;; :schema stream-schema
  :ns 'jiksnu.actions.stream-actions)

(defresource streams-api :item
  :desc "Resource routes for single Stream"
  :url "/{_id}"
  :parameters {:_id (path :model.stream/id)}
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)
                   item (model.stream/fetch-by-id id)]
               {:data item}))
  ;; :delete! #'actions.stream/delete
  ;; :put!    #'actions.stream/update-record
  )


(defn routes
  []
  [
   ;; [[:get  "/"]                                       #'actions.stream/public-timeline]

   [[:get  "/api/direct_messages.:format"]            #'actions.stream/direct-message-timeline]
   ;; FIXME: identicurse sends a post. seems wrong to me.
   [[:post "/api/direct_messages.:format"]            #'actions.stream/direct-message-timeline]

   [[:get  "/api/statuses/friends_timeline.:format"]  #'actions.stream/home-timeline]
   [[:get  "/api/statuses/home_timeline.:format"]     #'actions.stream/home-timeline]
   [[:get  "/api/statuses/mentions"]                  #'actions.stream/mentions-timeline]
   ;; [[:get    "/api/mentions"]                                     #'actions.stream/mentions-timeline]
   [[:get  "/api/statuses/public_timeline.:format"]   #'actions.stream/public-timeline]
   [[:get  "/api/statuses/user_timeline/:id"]         #'actions.stream/user-timeline]

   [[:get  "/api/user/:username/feed"]                {:action #'actions.stream/user-timeline      :format :as}]
   [[:post "/api/user/:username/feed"]                {:action #'actions.activity/post             :format :as}]
   [[:get  "/api/user/:username/inbox/direct/major"]  {:action #'actions.stream/direct-inbox-major :format :as}]
   [[:get  "/api/user/:username/inbox/direct/minor"]  {:action #'actions.stream/direct-inbox-minor :format :as}]
   [[:get  "/api/user/:username/inbox/major"]         {:action #'actions.stream/inbox-major        :format :as}]
   [[:get  "/api/user/:username/inbox/minor"]         {:action #'actions.stream/inbox-minor        :format :as}]

   ;; [[:post "/main/push/callback"]                     #'actions.stream/callback-publish]

   ;; [[:get  "/remote-user/*"]                          #'actions.stream/user-timeline]
   [[:post "/streams"]                                #'actions.stream/create]

   ;; [[:get  "/users/:id.:format"]                      #'actions.stream/user-timeline]
   ;; [[:get  "/users/:id"]                              #'actions.stream/user-timeline]

   ;; [[:get  "/:username.:format"]                      #'actions.stream/user-timeline]
   ;; [[:get  "/:username"]                              #'actions.stream/user-timeline]
   ;; [[:get  "/:username/all"]                          #'actions.stream/home-timeline]
   ;; [[:get  "/:username/streams"]                      #'actions.stream/user-list]
   ;; [[:post "/:username/streams"]                      #'actions.stream/add]
   ;; [[:get  "/:username/microsummary"]                 #'actions.stream/user-microsummary]
   ;; [[:get  "/:username/streams/new"]                  #'actions.stream/add-stream-page]

   ])

(defn pages
  []
  [
   [{:name "public-timeline"} {:action #'actions.stream/public-timeline}]
   [{:name "streams"}         {:action #'actions.stream/index}]
   ])

