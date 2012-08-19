(ns jiksnu.routes.activity-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]])
  (:require [jiksnu.actions.activity-actions :as activity]))

(add-route! "/notice/:id"      {:named "show activity"})
(add-route! "/notice/:id/edit" {:named "edit activity"})
(add-route! "/notice/new"      {:named "new activity"})
(add-route! "/main/oembed"     {:named "oembed"})

(defn routes
  []
  [
   [[:post   "/api/statuses/update.:format"]   #'activity/post]
   [[:get    "/api/statuses/show/:id.:format"] #'activity/show]
   [[:get    (named-path "oembed")]            #'activity/oembed]
   [[:get    "/notice/:id.:format"]            #'activity/show]
   [[:get    (named-path "show activity")]     #'activity/show]
   [[:post   (named-path "new activity")]      #'activity/post]
   [[:post   (named-path "show activity")]     #'activity/update]
   [[:delete "/notice/:id.:format"]            #'activity/delete]
   [[:delete "/notice/:id"]                    #'activity/delete]
   [[:get    (named-path "edit activity")]     #'activity/edit-page]
   ;; [[:get "/main/events"]                      #'activity/stream]
   ])
