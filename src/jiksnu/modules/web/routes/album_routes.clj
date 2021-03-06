(ns jiksnu.modules.web.routes.album-routes
  (:require [cemerick.friend :as friend]
            [ciste.config :refer [config]]
            [jiksnu.modules.core.actions.album-actions :as actions.album]
            [jiksnu.modules.http.resources :refer [add-group! defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource ciste-resource
                                                defparameter item-resource page-resource
                                                path subpage-resource]]
            [slingshot.slingshot :refer [throw+]]))

(defparameter :model.album/id
  :in :path
  :description "The Id of an album"
  :type "string")

(def album-schema
  {:id "Album"
   :type "object"
   :properties {:name {:type "string"}}})

;; =============================================================================

(defgroup jiksnu albums
  :name "Albums"
  :url "/main/albums")

(defresource albums :collection
  :methods {:get {:summary "Index Albums Page"}}
  :mixins [angular-resource])

(defresource albums :resource
  :url "/{_id}"
  :methods {:get {:summary "Show Album Page"}}
  :parameters {:_id (path :model.album/id)}
  :mixins [angular-resource])

;; =============================================================================

(defgroup jiksnu albums-api
  :name "Album Models"
  :url "/model/albums")

(defn albums-api-post
  [ctx]
  (let [{{params :params
          :as request} :request} ctx
        username (:current (friend/identity request))
        id (str "acct:" username "@" (config :domain))
        params (assoc params :owner id)]
    (actions.album/post params)))

(defresource albums-api :collection
  :desc "Collection route for albums"
  :mixins [page-resource]
  :available-formats [:json]
  :allowed-methods [:get :post]
  :page "albums"
  :available-media-types ["application/json"]
  :methods {:get {:summary "Index Albums"}
            :post {:summary "Create Album"}}
  :post! albums-api-post
  :schema album-schema
  :ns 'jiksnu.modules.core.actions.album-actions)

(defresource albums-api :item
  :desc "Resource routes for single Album"
  :url "/{_id}"
  :parameters {:_id (path :model.album/id)}
  :methods {:get {:summary "Show Album"}
            :delete {:summary "Delete Album"
                     :authenticated true}}
  :ns 'jiksnu.modules.core.actions.album-actions
  :mixins [item-resource])

(defresource albums-api :pictures
  :url "/{_id}/pictures"
  :name "Album Pictures"
  :description "Pictures of {{name}}"
  :mixins [subpage-resource]
  :target-model "album"
  :subpage "pictures"
  :parameters {:_id (path :model.album/id)})
