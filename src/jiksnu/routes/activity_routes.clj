(ns jiksnu.routes.activity-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes.helpers :only [formatted-path]])
  (:require [jiksnu.actions.activity-actions :as activity]))

(add-route! "/main/oembed"              {:named "oembed"})
(add-route! "/notice/:id"               {:named "show activity"})
(add-route! "/notice/:id"               {:named "delete activity"})
(add-route! "/notice/:id/edit"          {:named "edit activity"})
(add-route! "/notice/new"               {:named "new activity"})
(add-route! "/api/user/:username/feed"  {:named "activity outbox"})
(add-route! "/api/user/:username/inbox" {:named "activity inbox"})
(add-route! "/model/activities/:id"     {:named "activity model"})

(defn routes
  []
  [
   [[:post   "/api/statuses/update.:format"]     #'activity/post]
   [[:get    "/api/statuses/show/:id.:format"]   #'activity/show]
   [[:get    (named-path     "oembed")]          #'activity/oembed]
   [[:get    (formatted-path "show activity")]   #'activity/show]
   [[:get    (named-path     "show activity")]   #'activity/show]
   [[:post   (named-path     "new activity")]    #'activity/post]
   [[:post   (named-path     "show activity")]   #'activity/edit]
   [[:delete (formatted-path "delete activity")] #'activity/delete]
   [[:delete (named-path     "delete activity")] #'activity/delete]
   [[:get    (named-path     "edit activity")]   #'activity/edit-page]
   [[:get    (formatted-path "activity model")]  #'activity/show]
   ;; [[:get "/main/events"]                      #'activity/stream]
   ])

(defn pages
  []
  [
   [{:name "activities"}    {:action #'activity/index}]
   ])
