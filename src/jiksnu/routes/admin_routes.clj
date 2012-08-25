(ns jiksnu.routes.admin-routes
  (:use [ciste.routes :only [make-matchers]]
        [clojurewerkz.route-one.core :only [add-route! named-path]])
  (:require [jiksnu.actions.admin.activity-actions :as admin.activity]
            [jiksnu.actions.admin.auth-actions :as admin.auth]
            [jiksnu.actions.admin.conversation-actions :as admin.conversation]
            [jiksnu.actions.admin.group-actions :as admin.group]
            [jiksnu.actions.admin.feed-source-actions :as admin.feed-source]
            [jiksnu.actions.admin.feed-subscription-actions :as admin.feed-subscription]
            [jiksnu.actions.admin.like-actions :as admin.like]
            [jiksnu.actions.admin.key-actions :as admin.key]
            [jiksnu.actions.admin.setting-actions :as admin.setting]
            [jiksnu.actions.admin.subscription-actions :as admin.sub]
            [jiksnu.actions.admin.user-actions :as admin.user]
            [jiksnu.actions.admin.worker-actions :as admin.worker]
            [jiksnu.actions.admin-actions :as admin]))

(add-route! "/admin/activities"        {:named "admin activity index"})
(add-route! "/admin/conversations"     {:named "admin conversation index"})
(add-route! "/admin/feed-sources"      {:named "admin feed-source index"})

(add-route! "/admin/conversations/:id" {:named "admin show conversation"})
(add-route! "/admin/feed-sources/:id"  {:named "admin show feed-source"})
(add-route! "/admin/users/:id"         {:named "admin show user"})

(def admin-routes
  (make-matchers
   [
    [[:get    "/admin"]                                   #'admin/index]

    [[:get    "/admin/activities.:format"]                #'admin.activity/index]
    [[:get    (named-path "admin activity index")]        #'admin.activity/index]

    [[:get    "/admin/auth"]                              #'admin.auth/index]

    [[:get    (named-path "admin conversation index")]    #'admin.conversation/index]
    [[:get    "/admin/conversations.:format"]             #'admin.conversation/index]
    [[:post   (named-path "admin conversation index")]    #'admin.conversation/create]
    [[:get    (named-path "admin show conversation")]     #'admin.conversation/show]
    [[:get    "/admin/conversations/:id.:format"]         #'admin.conversation/show]
    [[:post   "/admin/conversations/:id/update"]          #'admin.conversation/fetch-updates]
    [[:post   "/admin/conversations/:id/delete"]          #'admin.conversation/delete]

    [[:get    "/admin/feed-sources.:format"]              #'admin.feed-source/index]
    [[:get    (named-path "admin feed-source index")]     #'admin.feed-source/index]
    [[:get    "/admin/feed-sources/:id.:format"]          #'admin.feed-source/show]
    [[:get    (named-path "admin show feed-source")]      #'admin.feed-source/show]
    [[:post   "/admin/feed-sources/:id/delete"]           #'admin.feed-source/delete]
    [[:post   "/admin/feed-sources/:id/unsubscribe"]      #'admin.feed-source/remove-subscription]
    [[:post   "/admin/feed-sources/:id/update"]           #'admin.feed-source/fetch-updates]
    [[:post   "/admin/feed-sources/:id/watchers"]         #'admin.feed-source/add-watcher]
    [[:post   "/admin/feed-sources/:id/watchers/delete"]  #'admin.feed-source/remove-watcher]

    [[:get    "/admin/feed-subscriptions.:format"]        #'admin.feed-subscription/index]
    [[:get    "/admin/feed-subscriptions"]                #'admin.feed-subscription/index]

    [[:get    "/admin/groups.:format"]                    #'admin.group/index]
    [[:get    "/admin/groups"]                            #'admin.group/index]
    [[:post   "/admin/groups"]                            #'admin.group/create]
    [[:get    "/admin/groups/:id.:format"]                #'admin.group/show]
    [[:get    "/admin/groups/:id"]                        #'admin.group/show]
    [[:post   "/admin/groups/:id/delete"]                 #'admin.group/delete]

    [[:get    "/admin/likes.:format"]                     #'admin.like/index]
    [[:get    "/admin/likes"]                             #'admin.like/index]
    [[:delete "/admin/likes/:id.:format"]                 #'admin.like/delete]
    [[:delete "/admin/likes/:id"]                         #'admin.like/delete]
    [[:post   "/admin/likes/:id/delete"]                  #'admin.like/delete]
        
    [[:get    "/admin/keys.:format"]                      #'admin.key/index]
    [[:get    "/admin/keys"]                              #'admin.key/index]
    [[:post   "/admin/keys"]                              #'admin.key/create]
    [[:get    "/admin/keys/:id.:format"]                  #'admin.key/show]
    [[:get    "/admin/keys/:id"]                          #'admin.key/show]
    [[:post   "/admin/keys/:id/delete"]                   #'admin.key/delete]
    
    [[:get    "/admin/subscriptions.:format"]             #'admin.sub/index]
    [[:get    "/admin/subscriptions"]                     #'admin.sub/index]
    [[:post   "/admin/subscriptions"]                     #'admin.sub/create]
    [[:get    "/admin/subscriptions/:id.:format"]         #'admin.sub/show]
    [[:get    "/admin/subscriptions/:id"]                 #'admin.sub/show]
    [[:post   "/admin/subscriptions/:id/delete"]          #'admin.sub/delete]
    [[:post   "/admin/subscriptions/:id/update"]          #'admin.sub/update]

    [[:post   "/admin/users"]                             #'admin.user/create]
    [[:get    "/admin/users.:format"]                     #'admin.user/index]
    [[:get    "/admin/users"]                             #'admin.user/index]
    [[:get    "/admin/users/:id.:format"]                 #'admin.user/show]
    [[:get    (named-path "admin show user")]             #'admin.user/show]

    [[:get    "/admin/settings"]                          #'admin.setting/edit-page]
    [[:post   "/admin/settings"]                          #'admin.setting/update-settings]

    [[:get    "/admin/workers.:format"]                   #'admin.worker/index]
    [[:get    "/admin/workers"]                           #'admin.worker/index]
    [[:post   "/admin/workers/start"]                     #'admin.worker/start-worker]
    [[:post   "/admin/workers/stop"]                      #'admin.worker/stop-worker]
    [[:post   "/admin/workers/stop/all"]                  #'admin.worker/stop-all-workers]
    ]))

