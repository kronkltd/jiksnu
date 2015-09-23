(ns jiksnu.modules.web.routes.activity-routes
  (:require [cemerick.friend :as friend]
            [ciste.config :refer [config]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.modules.http.resources :refer [add-group! defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource path]]
            [octohipster.mixins :as mixin]))

(defparameter :model.activity/id
  :in :path
  :description "The Id of an activity"
  :type "string")

(def activity-schema
  {:id "Activity"
   :type "object"
   :properties {:content {:type "string"}}})

;; =============================================================================

(defgroup activities
  :name "Activities"
  :url "/activities")

(add-group! #'jiksnu #'activities)

(defresource activities :collection
  :methods {:get {:summary "Index Activities Page"}}
  :mixins [angular-resource])

(defresource activities :resource
  :url "/{_id}"
  :methods {:get {:summary "Show Activity Page"}}
  :parameters {:_id (path :model.activity/id)}
  :mixins [angular-resource])

;; =============================================================================

(defgroup activities-api
  :name "Activities API"
  :url "/model/activities")

(add-group! #'jiksnu #'activities-api)

(defn activities-api-post
  [ctx]
  (let [{{params :params
          :as request} :request} ctx
          username (:current (friend/identity request))
          id (str "acct:" username "@" (config :domain))
          params (assoc params :author id)]
    (actions.activity/post params)))

(defresource activities-api :collection
  :desc "Collection route for activities"
  :mixins [page-resource]
  :available-formats [:json]
  :allowed-methods [:get :post]
  :methods {:get {:summary "Index Activities"}
            :post {:summary "Create Activity"}}
  :post! activities-api-post
  :schema activity-schema
  :ns 'jiksnu.actions.activity-actions)

(defresource activities-api :item
  :desc "Resource routes for single Activity"
  :url "/{_id}"
  :parameters {:_id (path :model.activity/id)}
  :methods {:get {:summary "Show Activity"}
            :delete {:summary "Delete Activity"}}
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)
                   activity (model.activity/fetch-by-id id)]
               {:data activity}))
  ;; :put!    #'actions.activity/update-record
  :delete! #'actions.activity/delete)
