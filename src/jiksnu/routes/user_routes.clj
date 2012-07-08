(ns jiksnu.routes.user-routes
  (:require [jiksnu.actions.user-actions :as user]))

(defn routes
  []
  [[[:get    "/api/friendships/exists.:format"]              #'user/exists?]
   [[:get    "/api/people/@me/@all"]                         #'user/index]
   [[:get    "/api/people/@me/@all/:id"]                     #'user/show]
   [[:get    "/main/register"]                               #'user/register-page]
   [[:post   "/main/register"]                               #'user/register]
   [[:get    "/main/xrd"]                                    #'user/user-meta]
   [[:get    "/main/profile"]                                #'user/profile]
   [[:post   "/main/profile"]                                #'user/update-profile]
   [[:get    "/users.:format"]                               #'user/index]
   [[:get    "/users"]                                       #'user/index]
   [[:delete "/users/:id"]                                   #'user/delete]
   [[:post   "/users/:id/delete"]                            #'user/delete]
   [[:post   "/users/:id/discover.:format"]                  #'user/discover]
   [[:post   "/users/:id/discover"]                          #'user/discover]
   [[:post   "/users/:id/update.:format"]                    #'user/fetch-updates]
   [[:post   "/users/:id/update"]                            #'user/fetch-updates]
   [[:post   "/users/:id/update-hub"]                        #'user/update-hub]
   [[:post   "/:username"]                                   #'user/update]])
