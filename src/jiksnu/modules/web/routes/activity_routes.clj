(ns jiksnu.modules.web.routes.activity-routes
  (:require [cemerick.friend :as friend]
            [ciste.config :refer [config]]
            [jiksnu.modules.core.actions.activity-actions :as actions.activity]
            [jiksnu.modules.core.actions.album-actions :as actions.album]
            [jiksnu.modules.core.actions.picture-actions :as actions.picture]
            [jiksnu.modules.http.resources :refer [add-group! defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource ciste-resource
                                                defparameter item-resource page-resource path
                                                subpage-resource]]
            [slingshot.slingshot :refer [throw+]]))

(defparameter :model.activity/id
  :in :path
  :description "The Id of an activity"
  :type "string")

(def activity-schema
  {:id "Activity"
   :type "object"
   :properties {:content {:type "string"}}})

;; =============================================================================

(defgroup jiksnu activities
  :name "Activities"
  :url "/main/activities")

(defresource activities :collection
  :methods {:get {:summary "Index Activities Page"}}
  :mixins [angular-resource])

(defresource activities :resource
  :url "/{_id}"
  :methods {:get {:summary "Show Activity Page"}}
  :parameters {:_id (path :model.activity/id)}
  :mixins [angular-resource])

;; =============================================================================

(defgroup jiksnu activities-api
  :name "Activity Models"
  :url "/model/activities")

(def default-album "* uploads")

(defn get-default-album
  [id]
  (or (first (:items (actions.album/fetch-by-user {:_id id} default-album)))
      (throw+ {:message "Could not determine default photo album"})))

(defn process-pictures
  [{id :author pictures :pictures :as params}]
  (if-let [picture-ids (when pictures
                         (if-let [album-id (get-default-album id)]
                           (map #(:_id (actions.picture/upload id album-id %)) pictures)))]
    (assoc params :pictures picture-ids)
    (dissoc params :pictures)))

(defn activities-api-post
  [ctx]
  (let [{{params :params :as request} :request} ctx
        username (:current (friend/identity request))
        id (str "acct:" username "@" (config :domain))
        params (-> params (assoc :author id) process-pictures)]
    (actions.activity/post params)))

(defresource activities-api :collection
  :desc "Collection route for activities"
  :mixins [page-resource]
  :available-formats [:json]
  :page "activities"
  :allowed-methods [:get :post]
  :available-media-types ["application/json"]
  :methods {:get {:summary "Index Activities"}
            :post {:summary "Create Activity"}}
  :post! activities-api-post
  :schema activity-schema
  :ns 'jiksnu.modules.core.actions.activity-actions)

(defresource activities-api :item
  :desc "Resource routes for single Activity"
  :url "/{_id}"
  :parameters {:_id (path :model.activity/id)}
  :methods {:get {:summary "Show Activity"}
            :delete {:summary "Delete Activity"
                     :authenticated true}}
  :mixins [item-resource]
  :target-model "activity"
  :ns 'jiksnu.modules.core.actions.activity-actions)

(defresource activities-api :likes
  :url "/{_id}/likes"
  :name "activity likes"
  :description "Likes of {{id}}"
  :mixins [subpage-resource]
  :target-model "activity"
  :subpage "likes"
  :parameters {:_id (path :model.activity/id)})
