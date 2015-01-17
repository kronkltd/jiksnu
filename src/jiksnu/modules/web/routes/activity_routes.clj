(ns jiksnu.modules.web.routes.activity-routes
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as activity]
            [jiksnu.modules.web.routes :as r]
            [octohipster.core :refer [defresource defgroup]]
            [octohipster.mixins :as mixin]))

(defresource activity-collection
  :desc "Collection route for activities"
  :mixins [mixin/collection-resource]
  ;; :count #'activity/count
  :exists? #'activity/index
  )

(defresource activity-resource
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

(defgroup activity-group
  :url "/activities"
  :resources [activity-collection activity-resource]
  )

(defn on-loaded
  []
  (log/info "adding activity group")
  (dosync
   (alter r/groups conj activity-group)))

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

