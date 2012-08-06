(ns jiksnu.routes.activity-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]])
  (:require [jiksnu.actions.activity-actions :as activity]))

(add-route! "/notice/:id" {:named "show activity"})

(defn routes
  []
  [
   [[:post   "/api/statuses/update.:format"]   #'activity/post]
   [[:get    "/api/statuses/show/:id.:format"] #'activity/show]
   [[:get    "/main/oembed"]                   #'activity/oembed]
   [[:get    "/notice/:id.:format"]            #'activity/show]
   [[:get    (named-path "show activity")]     #'activity/show]
   [[:post   "/notice/new"]                    #'activity/post]
   [[:post   "/notice/:id"]                    #'activity/update]
   [[:delete "/notice/:id"]                    #'activity/delete]
   [[:get    "/notice/:id/edit"]               #'activity/edit-page]
   ;; [[:get "/main/events"]                      #'activity/stream]
   ])
