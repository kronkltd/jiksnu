(ns jiksnu.routes.user-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]])
  (:require [jiksnu.actions.user-actions :as user]))

(add-route! "/main/register"      {:named "register page"})
(add-route! "/users"              {:named "index users"})
(add-route! "/users/:id/discover" {:named "discover user"})
(add-route! "/users/:id/update"   {:named "update user"})
(add-route! "/main/profile"       {:named "user profile"})

(defn routes
  []
  [[[:get    "/api/friendships/exists.:format"] #'user/exists?]
   [[:get    "/api/people/@me/@all"]            #'user/index]
   [[:get    "/api/people/@me/@all/:id"]        #'user/show]
   [[:get    (named-path "register page")]      #'user/register-page]
   [[:post   "/main/register"]                  #'user/register]
   [[:get    "/main/xrd"]                       #'user/user-meta]
   [[:get    (named-path "user profile")]       #'user/profile]
   [[:post   "/main/profile"]                   #'user/update-profile]
   [[:get    "/users.:format"]                  #'user/index]
   [[:get    (named-path "index users")]        #'user/index]
   [[:delete "/users/:id"]                      #'user/delete]
   [[:post   "/users/:id/delete"]               #'user/delete]
   [[:post   "/users/:id/discover.:format"]     #'user/discover]
   [[:post   (named-path "discover user")]      #'user/discover]
   [[:post   "/users/:id/update.:format"]       #'user/fetch-updates]
   [[:post   (named-path "update user")]        #'user/fetch-updates]
   ;; [[:post   "/users/:id/update-hub"]           #'user/update-hub]
   [[:post   "/:username"]                      #'user/update]
   [[:get "/model/users/:id.:format"]           #'user/show]])
