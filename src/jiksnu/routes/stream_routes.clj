(ns jiksnu.routes.stream-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]])
  (:require [jiksnu.actions.stream-actions :as stream]))

(add-route! "/" {:named "public timeline"})
(add-route! "/:username" {:named "local user timeline"})
(add-route! "/:username/all" {:named "home timeline"})


(defn routes
  []
  [
   [[:get    (named-path "public timeline")]            #'stream/public-timeline]
   [[:get    "/groups/:name"]                           #'stream/group-timeline]
   [[:get    "/users/:id.:format"]                      #'stream/user-timeline]
   [[:get    "/users/:id"]                              #'stream/user-timeline]
   [[:get    "/:username.:format"]                      #'stream/user-timeline]
   [[:get    (named-path "local user timeline")]        #'stream/user-timeline]
   [[:get    (named-path "home timeline")]              #'stream/home-timeline]
   [[:get    "/:username/streams"]                      #'stream/user-list]
   [[:post   "/:username/streams"]                      #'stream/add]
   [[:get    "/:username/microsummary"]                 #'stream/user-microsummary]
   [[:get    "/:username/streams/new"]                  #'stream/add-stream-page]
   [[:post   "/main/push/callback"]                     #'stream/callback-publish]
   [[:get    "/remote-user/*"]                          #'stream/user-timeline]
   [[:get    "/api/statuses/friends_timeline.:format"]  #'stream/home-timeline]
   [[:get    "/api/statuses/home_timeline.:format"]     #'stream/home-timeline]
   [[:get    "/api/statuses/mentions.:format"]          #'stream/mentions-timeline]
   [[:get    "/api/direct_messages.:format"]            #'stream/direct-message-timeline]
   ;; FIXME: identicurse sends a post. seems wrong to me.
   [[:post   "/api/direct_messages.:format"]            #'stream/direct-message-timeline]
   ;; [[:get    "/api/mentions"]                           #'stream/mentions-timeline]
   [[:get    "/api/statuses/public_timeline.:format"]   #'stream/public-timeline]
   [[:get    "/api/statuses/user_timeline/:id.:format"] #'stream/user-timeline]
   ])
