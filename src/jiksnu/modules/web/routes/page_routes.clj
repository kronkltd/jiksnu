(ns jiksnu.modules.web.routes.page-routes
  (:require [jiksnu.modules.http.resources :refer [defresource defgroup]]
            jiksnu.modules.web.actions.page-actions
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource path]]
            ))

;; =============================================================================

(defgroup jiksnu pages-api
          :name "Pages API"
          :url "/model/pages")

(defresource pages-api :collection
             :mixins [page-resource]
             :available-formats [:json]
             :ns 'jiksnu.modules.web.actions.page-actions)

;(defresource pages-api :item
;             :desc "Resource routes for single Page"
;             :url "/{_id}"
;             :parameters {:_id (path :model.domain/id)}
;             :mixins [mixin/item-resource]
;             :available-media-types ["application/json"]
;             :presenter (partial into {})
;             :exists? (fn [ctx]
;                        (let [id (-> ctx :request :route-params :_id)
;                              activity (model.domain/fetch-by-id id)]
;                          {:data activity}))
;             :delete! #'actions.domain/delete
;             ;; :put!    #'actions.domain/update-record
;             )
;
