(ns jiksnu.modules.web.routes.picture-routes
  (:require [ciste.config :refer [config]]
            [clojure.java.io :as io]
            [jiksnu.actions.picture-actions :as actions.picture]
            [jiksnu.model.picture :as model.picture]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.http.resources :refer [add-group! defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource path]]
            [jiksnu.util :as util]
            [octohipster.mixins :as mixin]))

(defgroup jiksnu pictures
  :name "Pictures"
  :url "/main/pictures")

(defresource pictures :collection
  :methods {:get {:summary "Index Pictures Page"}}
  :mixins [angular-resource])

(defresource pictures :resource
  :url "/{_id}"
  :methods {:get {:summary "Show picture Page"}}
  :parameters {:_id (path :model.picture/id)}
  :mixins [angular-resource])

;; =============================================================================

(defgroup jiksnu pictures-api
  :name "Picture Models"
  :url "/model/pictures")

(defresource pictures-api :collection
  :desc "Collection route for pictures"
  :mixins [page-resource]
  :available-formats [:json]
  :allowed-methods [:get :post]
  :available-media-types ["application/json"]
  :methods {:get {:summary "Index Pictures"}
            :post {:summary "Create Picture"}}
  :post! (fn [{{:keys [params] :as request} :request :as ctx}]
           (let [username (get-in ctx [:request :session :cemerick.friend/identity :current])
                 user (model.user/get-user username)
                 filename (:filename (:file params))
                 src (:tempfile (:file params))
                 dest (io/file "/data" filename)]
             (actions.picture/create
              {:filename filename
               :album (util/make-id (:album params))
               :user (:_id user)})
             (io/copy src dest)))
  ;; :schema picture-schema
  :ns 'jiksnu.actions.picture-actions)

(defresource pictures-api :item
  :desc "Resource routes for single Picture"
  :url "/{_id}"
  :parameters {:_id (path :model.picture/id)}
  :methods {:get {:summary "Show Picture"}
            :delete {:summary "Delete Picture"}}
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)
                   picture (model.picture/fetch-by-id id)]
               {:data picture}))
  ;; :put!    #'actions.picture/update-record
  :delete! (fn [ctx] (actions.picture/delete (:data ctx))))
