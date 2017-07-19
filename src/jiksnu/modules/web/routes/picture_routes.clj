(ns jiksnu.modules.web.routes.picture-routes
  (:require [ciste.config :refer [config]]
            [jiksnu.modules.core.actions.picture-actions :as actions.picture]
            [jiksnu.modules.core.model.user :as model.user]
            [jiksnu.modules.http.resources :refer [add-group! defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter item-resource
                                                page-resource path]]
            [slingshot.slingshot :refer [throw+]]))

(defparameter :model.picture/id
  :description "The Id of a Picture"
  :type "string")

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
  :page "pictures"
  :methods {:get {:summary "Index Pictures"}
            :post {:summary "Create Picture"}}
  :available-formats [:json]
  :post! (fn [{{:keys [params]} :request :as ctx}]
           (let [username (get-in ctx [:request :session :cemerick.friend/identity :current])]
             (if-let [user (model.user/get-user username)]
               (let [files (:files params)]
                 (doseq [file files]
                   (actions.picture/upload (:_id user) (:album params) file)))
               (throw+ {:body "Could not get user"}))))
  ;; :schema picture-schema
  :ns 'jiksnu.modules.core.actions.picture-actions)

(defresource pictures-api :item
  :desc "Resource routes for single Picture"
  :url "/{_id}"
  :ns 'jiksnu.modules.core.actions.picture-actions
  :parameters {:_id (path :model.picture/id)}
  :methods {:get {:summary "Show Picture"}
            :delete {:summary "Delete Picture"
                     :authenticated true}}
  :mixins [item-resource])
