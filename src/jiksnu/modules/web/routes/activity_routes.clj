(ns jiksnu.modules.web.routes.activity-routes
  (:require [cemerick.friend :as friend]
            [ciste.config :refer [config]]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.modules.http.resources :refer [add-group! defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource ciste-resource
                                                defparameter page-resource path]]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [octohipster.mixins :as mixin]
            [slingshot.slingshot :refer [try+ throw+]]
            [taoensso.timbre :as timbre]))

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
  :name "Activities API"
  :url "/model/activities")

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
  :available-media-types ["application/json"]
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
            :delete {:summary "Delete Activity"
                     :authenticated true}}
  :mixins [ciste-resource]
  :available-media-types ["application/json"]
  :available-formats [:json]
  ;; :presenter (partial into {})
  :authorized? (fn [ctx]
                 (if (#{:delete} (util/inspect (get-in ctx [:request :request-method])))
                   (do
                     (timbre/debug "authenticated method")
                     (not (nil? (util/inspect (session/current-user-id)))))
                   (do
                     (timbre/info "unauthenticated method")

                     ctx)))
  :allowed-methods [:get :delete]
  :exists? (fn [ctx]
             (let [id (get-in ctx [:request :route-params :_id])
                   activity (model.activity/fetch-by-id id)]
               {:data activity}))
  ;; :put!    #'actions.activity/update-record
  :delete! (fn [ctx]
             ;; (try+
              (actions.activity/delete (:data ctx))
              ;; (catch Object ex
              ;;   (timbre/error ex "Delete Error")
              ;;   )
             ;; )
  ))
