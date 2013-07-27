(ns jiksnu.routes.admin-routes
  (:use [ciste.commands :only [add-command!]]
        [ciste.routes :only [make-matchers]]
        [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes.helpers :only [formatted-path]])
  (:require [jiksnu.modules.admin.actions.activity-actions :as admin.activity]
            [jiksnu.modules.admin.actions.auth-actions :as admin.auth]
            [jiksnu.modules.admin.actions.conversation-actions :as admin.conversation]
            [jiksnu.modules.admin.actions.group-actions :as admin.group]
            [jiksnu.modules.admin.actions.feed-source-actions :as admin.feed-source]
            [jiksnu.modules.admin.actions.feed-subscription-actions :as admin.feed-subscription]
            [jiksnu.modules.admin.actions.like-actions :as admin.like]
            [jiksnu.modules.admin.actions.key-actions :as admin.key]
            [jiksnu.modules.admin.actions.setting-actions :as admin.setting]
            [jiksnu.actions.admin.stream-actions :as admin.stream]
            [jiksnu.modules.admin.actions.subscription-actions :as admin.sub]
            [jiksnu.modules.admin.actions.user-actions :as admin.user]
            [jiksnu.modules.admin.actions.worker-actions :as admin.worker]))

(add-route! "/admin/activities"        {:named "admin activity index"})
(add-route! "/admin/conversations"     {:named "admin conversation index"})
(add-route! "/admin/feed-sources"      {:named "admin feed-source index"})

(add-route! "/admin/conversations/:id" {:named "admin show conversation"})
(add-route! "/admin/feed-sources/:id"  {:named "admin show feed-source"})
(add-route! "/admin/users/:id"         {:named "admin show user"})

(def admin-routes
  (make-matchers
   [
    [[:get    "/admin/activities.:format"]                 #'admin.activity/index]
    [[:get    (named-path "admin activity index")]         #'admin.activity/index]

    [[:get    "/admin/auth"]                               #'admin.auth/index]
    [[:get    "/admin/auth.:format"]                       #'admin.auth/index]

    [[:get    (named-path "admin conversation index")]     #'admin.conversation/index]
    [[:get    (formatted-path "admin conversation index")] #'admin.conversation/index]
    [[:post   (named-path "admin conversation index")]     #'admin.conversation/create]
    [[:get    (named-path "admin show conversation")]      #'admin.conversation/show]
    [[:get    "/admin/conversations/:id.:format"]          #'admin.conversation/show]
    [[:post   "/admin/conversations/:id/update"]           #'admin.conversation/fetch-updates]
    [[:post   "/admin/conversations/:id/delete"]           #'admin.conversation/delete]

    [[:get    (formatted-path "admin feed-source index")]  #'admin.feed-source/index]
    [[:get    (named-path "admin feed-source index")]      #'admin.feed-source/index]
    [[:get    (formatted-path "admin show feed-source")]   #'admin.feed-source/show]
    [[:get    (named-path "admin show feed-source")]       #'admin.feed-source/show]
    [[:post   "/admin/feed-sources/:id/delete"]            #'admin.feed-source/delete]
    [[:post   "/admin/feed-sources/:id/unsubscribe"]       #'admin.feed-source/unsubscribe]
    [[:post   "/admin/feed-sources/:id/update"]            #'admin.feed-source/fetch-updates]
    [[:post   "/admin/feed-sources/:id/watchers"]          #'admin.feed-source/add-watcher]
    [[:post   "/admin/feed-sources/:id/watchers/delete"]   #'admin.feed-source/remove-watcher]

    [[:get    "/admin/feed-subscriptions.:format"]         #'admin.feed-subscription/index]
    [[:get    "/admin/feed-subscriptions"]                 #'admin.feed-subscription/index]

    [[:get    "/admin/groups.:format"]                     #'admin.group/index]
    [[:get    "/admin/groups"]                             #'admin.group/index]
    [[:post   "/admin/groups"]                             #'admin.group/create]
    [[:get    "/admin/groups/:id.:format"]                 #'admin.group/show]
    [[:get    "/admin/groups/:id"]                         #'admin.group/show]
    [[:post   "/admin/groups/:id/delete"]                  #'admin.group/delete]

    [[:get    "/admin/likes.:format"]                      #'admin.like/index]
    [[:get    "/admin/likes"]                              #'admin.like/index]
    [[:delete "/admin/likes/:id.:format"]                  #'admin.like/delete]
    [[:delete "/admin/likes/:id"]                          #'admin.like/delete]
    [[:post   "/admin/likes/:id/delete"]                   #'admin.like/delete]

    [[:get    "/admin/keys.:format"]                       #'admin.key/index]
    [[:get    "/admin/keys"]                               #'admin.key/index]
    [[:post   "/admin/keys"]                               #'admin.key/create]
    [[:get    "/admin/keys/:id.:format"]                   #'admin.key/show]
    [[:get    "/admin/keys/:id"]                           #'admin.key/show]
    [[:post   "/admin/keys/:id/delete"]                    #'admin.key/delete]

    [[:get    "/admin/subscriptions.:format"]              #'admin.sub/index]
    [[:get    "/admin/subscriptions"]                      #'admin.sub/index]
    [[:post   "/admin/subscriptions"]                      #'admin.sub/create]
    [[:get    "/admin/subscriptions/:id.:format"]          #'admin.sub/show]
    [[:get    "/admin/subscriptions/:id"]                  #'admin.sub/show]
    [[:post   "/admin/subscriptions/:id/delete"]           #'admin.sub/delete]
    [[:post   "/admin/subscriptions/:id/update"]           #'admin.sub/update]

    [[:post   "/admin/users"]                              #'admin.user/create]
    [[:get    "/admin/users.:format"]                      #'admin.user/index]
    [[:get    "/admin/users"]                              #'admin.user/index]
    [[:get    "/admin/users/:id.:format"]                  #'admin.user/show]
    [[:get    (named-path "admin show user")]              #'admin.user/show]

    [[:get    "/admin/settings"]                           #'admin.setting/edit-page]
    [[:post   "/admin/settings"]                           #'admin.setting/update-settings]

    [[:get    "/admin/streams"]                            #'admin.stream/index]

    [[:get    "/admin/workers.:format"]                    #'admin.worker/index]
    [[:get    "/admin/workers"]                            #'admin.worker/index]
    [[:post   "/admin/workers/start"]                      #'admin.worker/start-worker]
    [[:post   "/admin/workers/stop"]                       #'admin.worker/stop-worker]
    [[:post   "/admin/workers/stop/all"]                   #'admin.worker/stop-all-workers]
    ]))

(add-command! "list-workers" #'admin.worker/index)
(add-command! "start-worker" #'admin.worker/start-worker)
(add-command! "stop-worker"  #'admin.worker/stop-worker)
