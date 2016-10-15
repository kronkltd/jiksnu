(ns jiksnu.modules.web.routes.stream-routes
  (:require [cemerick.friend :as friend]
            [ciste.commands :refer [add-command!]]
            [ciste.core :refer [with-context]]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.model.stream :as model.stream]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter item-resource
                                                page-resource path subpage-resource]]
            [liberator.representation :refer [as-response ring-response]]
            [slingshot.slingshot :refer [throw+ try+]]
            [taoensso.timbre :as timbre]))

(defn get-stream
  "Gets the item from context by id"
  [ctx]
  (let [id (-> ctx :request :route-params :_id)]
    (model.stream/fetch-by-id id)))

(defparameter :model.stream/id
  :in :path
  :description "The Id of an stream"
  :type "string")

(defgroup jiksnu streams
  :name "Streams"
  :url "/main/streams")

(defresource streams :collection
  :mixins [angular-resource])

(defresource streams :item
  :url "/{_id}"
  :parameters {:_id (path :model.stream/id)}
  :mixins [angular-resource])

;; =============================================================================

(defgroup jiksnu streams-api
  :name "Stream Models"
  :url "/model/streams")

(defn post-stream
  [{{:keys [params]
     :as request} :request}]
  (try+
   (if-let [owner (model.user/get-user (:current (friend/identity request)))]
     (let [params (assoc params :owner (:_id owner))]
       (if-let [stream (actions.stream/create params)]
         {:data (:_id stream)}
         (throw+ {:message "Failed to create stream"
                  :type :failure})))
     (throw+ {:message "not authenticated"
              :type :authentication}))
   (catch [] ex
     (timbre/error ex)
     (ring-response ex {:status 500}))))

(defresource streams-api :collection
  :desc "Collection route for streams"
  :mixins [page-resource]
  :post! post-stream
  :post-redirect? (fn [ctx] {:location (format "/model/streams/%s" (:data ctx))})
  :schema {:type "object"
           :properties {:name {:type "string"}}
           :required [:name]}
  :ns 'jiksnu.actions.stream-actions)

(defresource streams-api :item
  :desc "Resource routes for single Stream"
  :url "/{_id}"
  :ns 'jiksnu.actions.stream-actions
  :parameters {:_id (path :model.stream/id)}
  :mixins [item-resource])

(defresource streams-api :activities
  :desc "activities in stream"
  :description "Activities of {{name}}"
  :url "/{_id}/activities"
  :name "stream activities"
  :parameters  {:_id  (path :model.stream/id)}
  :mixins [subpage-resource]
  :target get-stream
  :target-model "stream"
  :subpage "activities")
