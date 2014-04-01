(ns jiksnu.routes.stream-routes
  (:require [ciste.commands :refer [add-command!]]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.routes.helpers :refer [add-route! formatted-path named-path]]))

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
   [[:post "/streams"]                                  #'actions.stream/create]
   [[:get    (named-path     "public timeline")]        #'actions.stream/public-timeline]
   [[:get    (named-path     "group profile")]          #'actions.stream/group-timeline]
   [[:get    (formatted-path "user profile")]           #'actions.stream/user-timeline]
   [[:get    (named-path     "user profile")]           #'actions.stream/user-timeline]
   [[:get    (formatted-path "local user timeline")]    #'actions.stream/user-timeline]
   [[:get    (named-path     "local user timeline")]    #'actions.stream/user-timeline]
   [[:get    (named-path     "home timeline")]          #'actions.stream/home-timeline]
   [[:get    (named-path     "user stream index")]      #'actions.stream/user-list]
   [[:get    (named-path     "user microsummary")]      #'actions.stream/user-microsummary]
   [[:post   (named-path     "callback publish")]       #'actions.stream/callback-publish]
   [[:get    (formatted-path "mentions timeline api")]  #'actions.stream/mentions-timeline]
   [[:post   "/:username/streams"]                      #'actions.stream/add]
   [[:get    "/:username/streams/new"]                  #'actions.stream/add-stream-page]
   [[:get    "/remote-user/*"]                          #'actions.stream/user-timeline]
   [[:get    "/api/statuses/friends_timeline.:format"]  #'actions.stream/home-timeline]
   [[:get    "/api/statuses/home_timeline.:format"]     #'actions.stream/home-timeline]
   [[:get    "/api/direct_messages.:format"]            #'actions.stream/direct-message-timeline]
   ;; FIXME: identicurse sends a post. seems wrong to me.
   [[:post   "/api/direct_messages.:format"]            #'actions.stream/direct-message-timeline]
   ;; [[:get    "/api/mentions"]                                     #'actions.stream/mentions-timeline]
   [[:get    "/api/statuses/public_timeline.:format"]   #'actions.stream/public-timeline]
   [[:get    (formatted-path "user timeline")]          #'actions.stream/user-timeline]

   [[:get "/api/user/:username/feed"] {:action #'actions.stream/user-timeline :format :as}]
   [[:post "/api/user/:username/feed"] {:action #'actions.activity/post :format :as}]

   [[:get    "/api/user/:username/inbox/major"]         {:action #'actions.stream/inbox-major
                                                         :format :as}]
   [[:get    "/api/user/:username/inbox/minor"]         {:action #'actions.stream/inbox-minor
                                                         :format :as}]
   [[:get "/api/user/:username/inbox/direct/major"] {:action #'actions.stream/direct-inbox-major
                                                     :format :as}]
   [[:get    "/api/user/:username/inbox/direct/minor"] {:action #'actions.stream/direct-inbox-minor
                                                        :format :as}]

   ])

(defn pages
  []
  [
   [{:name "public-timeline"} {:action #'actions.stream/public-timeline}]
   [{:name "streams"}         {:action #'actions.stream/index}]
   ])

