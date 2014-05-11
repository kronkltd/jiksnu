(ns jiksnu.routes.user-routes
  (:require [ciste.initializer :only [definitializer]]
            [jiksnu.actions.stream-actions :as stream]
            [jiksnu.actions.subscription-actions :as sub]
            [jiksnu.actions.user-actions :as user]
            [jiksnu.routes.helpers :refer [add-route! formatted-path named-path]])
  (:import jiksnu.model.User))

(add-route! "/users/:id/discover" {:named "discover user"})
(add-route! "/users/:id/update"   {:named "update user"})
(add-route! "/main/profile"       {:named "user profile"})
(add-route! "/main/xrd"           {:named "user meta"})
(add-route! "/model/users/:id"    {:named "user model"})

(defn routes
  []
  [
   [[:get    "/main/register"]  #'user/register-page]
   [[:post   "/main/register"]  #'user/register]
   [[:get    "/main/profile"]   #'user/profile]
   [[:post   "/main/profile"]   #'user/update-profile]
   [[:get    "/users.:format"]    #'user/index]
   [[:get    "/users"]    #'user/index]
   [[:post   "/users/:id/discover.:format"]  #'user/discover]
   [[:post   "/users/:id/discover"]  #'user/discover]
   [[:post   "/users/:id/update.:format"]    #'user/update]
   [[:post   "/users/:id/update"]    #'user/update]
   [[:get    "/main/xrd"]      #'user/user-meta]
   [[:get    "/model/users/:id"]     #'user/show]

   [[:get    "/api/friendships/exists.:format"] #'user/exists?]
   [[:get    "/api/people/@me/@all"]            #'user/index]
   [[:get    "/api/people/@me/@all/:id"]        #'user/show]
   [[:delete "/users/:id"]                      #'user/delete]
   [[:post "/users/:id/streams"]                #'user/add-stream]
   [[:post   "/users/:id/delete"]               #'user/delete]
   ;; [[:post   "/users/:id/update-hub"]           #'user/update-hub]
   [[:post   "/:username"]                      #'user/update]
   [[:get    "/api/user/:username/profile"]     {:action #'user/show
                                                 :format :as}]
   [[:get    "/api/user/:username/"]     {:action #'user/show-basic
                                                 :format :as}]
   ])

(defn pages
  []
  [
   [{:name "users"}         {:action #'user/index}]
   ])

(defn sub-pages
  []
  [
   [{:type User :name "activities"}       {:action #'stream/user-timeline}]
   [{:type User :name "subscriptions"}    {:action #'sub/get-subscriptions}]
   [{:type User :name "subscribers"}      {:action #'sub/get-subscribers}]
   [{:type User :name "streams"}          {:action #'stream/fetch-by-user}]

   ])
