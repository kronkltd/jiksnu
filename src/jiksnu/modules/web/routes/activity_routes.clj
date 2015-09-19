(ns jiksnu.modules.web.routes.activity-routes
  (:require [cemerick.friend :as friend]
            [ciste.config :refer [config]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers :refer [angular-resource page-resource]]
            [octohipster.mixins :as mixin]))

(def activity-schema
  {:id "Activity"
   :type "object"
   :properties {:content {:type "string"}}})

;; =============================================================================

(defgroup activities
  :name "Activities"
  :url "/activities")

(defresource activities collection
  :name "List Activities"
  :mixins [angular-resource])

(defresource activities resource
  :url "/{_id}"
  :parameters {"_id" {:name "Activity ID"
                      :in :path
                      :description "The ID of the activity"}}
  :mixins [angular-resource])

;; =============================================================================

(defgroup activities-api
  :name "Activities API"
  :url "/model/activities")

(defresource activities-api api-collection
  :desc "Collection route for activities"
  :mixins [page-resource]
  :available-formats [:json]
  :allowed-methods [:get :post]
  :post! (fn [ctx]
           (let [{{params :params
                   :as request} :request} ctx
                   username (:current (friend/identity request))
                   id (str "acct:" username "@" (config :domain))
                   params (assoc params :author id)]
             (actions.activity/post params)))
  :schema activity-schema
  :ns 'jiksnu.actions.activity-actions)

(defresource activities-api api-item
  :desc "Resource routes for single Activity"
  :url "/{_id}"
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)
                   activity (model.activity/fetch-by-id id)]
               {:data activity}))
  :delete! #'actions.activity/delete
  ;; :put!    #'actions.activity/update-record
  )

;; =============================================================================

(defn routes
  []
  [
   [[:post   "/api/statuses/update.:format"]   #'actions.activity/post]
   [[:get    "/api/statuses/show/:id.:format"] #'actions.activity/show]
   ;; [[:get    "/main/oembed"]                   #'actions.activity/oembed]
   [[:get    "/notice/:id.:format"]            #'actions.activity/show]
   [[:get    "/notice/:id"]                    #'actions.activity/show]
   [[:post   "/notice/new"]                    #'actions.activity/post]
   [[:post   "/notice/:id"]                    #'actions.activity/edit]
   [[:delete "/notice/:id.:format"]            #'actions.activity/delete]
   [[:delete "/notice/:id"]                    #'actions.activity/delete]
   ;; [[:get    "/notice/:id/edit"]               #'actions.activity/edit-page]
   ;; [[:get    "/model/activities/:id"]          #'actions.activity/show]
   ;; [[:get "/main/events"]                      #'actions.activity/stream]
   ])

(defn pages
  []
  [
   [{:name "activities"}    {:action #'actions.activity/index}]
   ])

