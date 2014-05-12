(ns jiksnu.modules.web.routes.stream-routes
  (:require [ciste.commands :refer [add-command!]]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.stream-actions :as actions.stream]))

(defn routes
  []
  [
   [[:post "/streams"]                                  #'actions.stream/create]
   [[:get    "/"]        #'actions.stream/public-timeline]
   [[:get    "/main/groups/:name"]          #'actions.stream/group-timeline]
   [[:get    "/users/:id.:format"]           #'actions.stream/user-timeline]
   [[:get    "/users/:id"]           #'actions.stream/user-timeline]
   [[:get    "/:username.:format"]    #'actions.stream/user-timeline]
   [[:get    "/:username"]    #'actions.stream/user-timeline]
   [[:get    "/:username/all"]          #'actions.stream/home-timeline]
   [[:get    "/:username/streams"]      #'actions.stream/user-list]
   [[:get    "/:username/microsummary"]      #'actions.stream/user-microsummary]
   [[:post   "/main/push/callback"]       #'actions.stream/callback-publish]
   [[:get    "/api/statuses/mentions"]  #'actions.stream/mentions-timeline]
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
   [[:get    "/api/statuses/user_timeline/:id"]          #'actions.stream/user-timeline]

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

