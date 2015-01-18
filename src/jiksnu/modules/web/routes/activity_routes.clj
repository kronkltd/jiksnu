(ns jiksnu.modules.web.routes.activity-routes
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as activity]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [octohipster.mixins :as mixin]))

(defgroup activities
  :url "/activities")

(defresource activities collection
  :desc "Collection route for activities"
  :mixins [mixin/collection-resource]
  :available-media-types ["application/json"]
  :count (fn [ctx]
           (log/info "counting activities")
           2
           )
  :data-key :activities
  :exists? (fn [ctx]
             {:page (log/spy :info (activity/index))})
  :handle-ok (fn [ctx]
               (log/spy :info ctx)
               (json/json-str (:page ctx))
               )

  )

(defresource activities item
  :desc "Resource routes for single Activity"
  :url "/{_id}"
  :mixins [mixin/item-resource]
  :exists? #'activity/show
  :delete! #'activity/delete
  ;; :put!    #'activity/update
  )

;; (defresource activity-post-page
;;   :desc ""
;;   )

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

