(ns jiksnu.modules.web.routes.stream-routes
  (:require [cemerick.friend :as friend]
            [ciste.commands :refer [add-command!]]
            [ciste.core :refer [with-context]]
            [ciste.sections.default :refer [index-section show-section]]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.model.stream :as model.stream]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter
                                                page-resource path
                                                subpage-resource]]
            [liberator.representation :refer [as-response ring-response]]
            [octohipster.mixins :as mixin]
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
  :name "Streams API"
  :url "/model/streams")

(defn post-stream
  [{{:keys [params]
     :as request} :request}]
  (try+
   (if-let [owner (model.user/get-user (:current (friend/identity request)))]
     (let [params (assoc params :owner (:_id owner))]
       (if-let [stream (actions.stream/create params {})]
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
  :available-formats [:json]
  :allowed-methods [:get :post]
  :post! post-stream
  :post-redirect? (fn [ctx]
                    {:location (format "/model/streams/%s" (:data ctx))})
  :schema {:type "object"
           :properties {:name {:type "string"}}
           :required [:name]}
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

(defresource streams-api :activities
  :desc "activities in stream"
  :description "Activities of {{name}}"
  :url "/{_id}/activities"
  :name "stream activities"
  :parameters  {:_id  (path :model.stream/id)}
  :mixins [subpage-resource]
  :target get-stream
  :target-model "stream"
  :subpage "activities"
  :allowed-methods [:get]
  :available-formats [:json]
  :available-media-types ["application/json"]
  ;:presenter :data #_(partial into {})
  ;; :presenter (fn [rsp]
  ;;              (merge (:body rsp)
  ;;                     {:displayName "Activities in Stream"}))
  )
