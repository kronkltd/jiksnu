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

(defn pages
  []
  [
   [{:name "public-timeline"} {:action #'actions.stream/public-timeline}]
   [{:name "streams"}         {:action #'actions.stream/index}]
   ])

