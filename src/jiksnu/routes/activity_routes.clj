(ns jiksnu.routes.activity-routes
  (:require [jiksnu.actions.activity-actions :as activity]))

(defn routes
  []
  [
   [[:post   "/api/statuses/update.:format"]                 #'activity/post]
   [[:get    "/api/statuses/show/:id.:format"]               #'activity/show]
   [[:get    "/main/oembed"]                                 #'activity/oembed]
   [[:get    "/notice/:id.:format"]                          #'activity/show]
   [[:get    "/notice/:id"]                                  #'activity/show]
   [[:post   "/notice/new"]                                  #'activity/post]
   [[:post   "/notice/:id"]                                  #'activity/update]
   [[:delete "/notice/:id"]                                  #'activity/delete]
   [[:get    "/notice/:id/edit"]                             #'activity/edit-page]
   ;; [[:get "/main/events"]                                     #'activity/stream]
   ])
