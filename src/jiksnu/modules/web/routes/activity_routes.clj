(ns jiksnu.modules.web.routes.activity-routes
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as activity]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers :refer [angular-resource page-resource]]
            [octohipster.mixins :as mixin]))

;; =============================================================================

(defgroup activities
  :url "/activities")

(defresource activities collection
  :mixins [angular-resource])

(defresource activities resource
  :url "/{_id}"
  :mixins [angular-resource])

;; (defresource activity-post-page
;;   :desc ""
;;   )

;; =============================================================================

(defgroup activities-api
  :url "/api/activities")

(defresource activities-api collection
  :desc "Collection route for activities"
  :mixins [page-resource]
  :ns 'jiksnu.actions.activity-actions)

(defresource activities-api item
  :desc "Resource routes for single Activity"
  :url "/{_id}"
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)
                   activity (model.activity/fetch-by-id id)]
               {:data activity}))
  :delete! #'activity/delete
  ;; :put!    #'activity/update
  )

;; =============================================================================

(defn routes
  []
  [
   [[:post   "/api/statuses/update.:format"]   #'activity/post]
   [[:get    "/api/statuses/show/:id.:format"] #'activity/show]
   ;; [[:get    "/main/oembed"]                   #'activity/oembed]
   [[:get    "/notice/:id.:format"]            #'activity/show]
   [[:get    "/notice/:id"]                    #'activity/show]
   [[:post   "/notice/new"]                    #'activity/post]
   [[:post   "/notice/:id"]                    #'activity/edit]
   [[:delete "/notice/:id.:format"]            #'activity/delete]
   [[:delete "/notice/:id"]                    #'activity/delete]
   ;; [[:get    "/notice/:id/edit"]               #'activity/edit-page]
   ;; [[:get    "/model/activities/:id"]          #'activity/show]
   ;; [[:get "/main/events"]                      #'activity/stream]
   ])

(defn pages
  []
  [
   [{:name "activities"}    {:action #'activity/index}]
   ])

