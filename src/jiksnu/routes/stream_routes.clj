(ns jiksnu.routes.stream-routes
  (:use [ciste.commands :only [add-command!]]
        [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes.helpers :only [formatted-path]])
  (:require [jiksnu.actions.stream-actions :as stream]))

(add-route! "/"                               {:named "public timeline"})
(add-route! "/:username"                      {:named "local user timeline"})
(add-route! "/:username/all"                  {:named "home timeline"})
(add-route! "/:username/microsummary"         {:named "user microsummary"})
(add-route! "/:username/streams"              {:named "user stream index"})
(add-route! "/main/push/callback"             {:named "callback publish"})
(add-route! "/api/statuses/mentions"          {:named "mentions timeline api"})
(add-route! "/users/:id"                      {:named "user profile"})
(add-route! "/groups/:name"                   {:named "group profile"})
(add-route! "/api/statuses/user_timeline/:id" {:named "user timeline"})

(defn routes
  []
  [
   [[:post "/streams"]                                  #'stream/create]
   [[:get    (named-path     "public timeline")]        #'stream/public-timeline]
   [[:get    (named-path     "group profile")]          #'stream/group-timeline]
   [[:get    (formatted-path "user profile")]           #'stream/user-timeline]
   [[:get    (named-path     "user profile")]           #'stream/user-timeline]
   [[:get    (formatted-path "local user timeline")]    #'stream/user-timeline]
   [[:get    (named-path     "local user timeline")]    #'stream/user-timeline]
   [[:get    (named-path     "home timeline")]          #'stream/home-timeline]
   [[:get    (named-path     "user stream index")]      #'stream/user-list]
   [[:get    (named-path     "user microsummary")]      #'stream/user-microsummary]
   [[:post   (named-path     "callback publish")]       #'stream/callback-publish]
   [[:get    (formatted-path "mentions timeline api")]  #'stream/mentions-timeline]
   [[:post   "/:username/streams"]                      #'stream/add]
   [[:get    "/:username/streams/new"]                  #'stream/add-stream-page]
   [[:get    "/remote-user/*"]                          #'stream/user-timeline]
   [[:get    "/api/statuses/friends_timeline.:format"]  #'stream/home-timeline]
   [[:get    "/api/statuses/home_timeline.:format"]     #'stream/home-timeline]
   [[:get    "/api/direct_messages.:format"]            #'stream/direct-message-timeline]
   ;; FIXME: identicurse sends a post. seems wrong to me.
   [[:post   "/api/direct_messages.:format"]            #'stream/direct-message-timeline]
   ;; [[:get    "/api/mentions"]                                     #'stream/mentions-timeline]
   [[:get    "/api/statuses/public_timeline.:format"]   #'stream/public-timeline]
   [[:get    (formatted-path "user timeline")]          #'stream/user-timeline]
   ])

(defn pages
  []
  [
   [{:name "public-timeline"} {:action #'stream/public-timeline}]
   [{:name "streams"}         {:action #'stream/index}]
   ])

