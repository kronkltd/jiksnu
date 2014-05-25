(ns jiksnu.modules.web.routes.admin-routes
  (:require [ciste.commands :refer [add-command!]]
            [ciste.routes :refer [make-matchers]]
            [jiksnu.modules.admin.actions.activity-actions :as admin.activity]
            [jiksnu.modules.admin.actions.auth-actions :as admin.auth]
            [jiksnu.modules.admin.actions.client-actions :as admin.client]
            [jiksnu.modules.admin.actions.conversation-actions :as admin.conversation]
            [jiksnu.modules.admin.actions.group-actions :as admin.group]
            [jiksnu.modules.admin.actions.group-membership-actions :as admin.group-membership]
            [jiksnu.modules.admin.actions.feed-source-actions :as admin.feed-source]
            [jiksnu.modules.admin.actions.feed-subscription-actions :as admin.feed-subscription]
            [jiksnu.modules.admin.actions.like-actions :as admin.like]
            [jiksnu.modules.admin.actions.key-actions :as admin.key]
            [jiksnu.modules.admin.actions.request-token-actions :as admin.request-token]
            [jiksnu.modules.admin.actions.setting-actions :as admin.setting]
            [jiksnu.modules.admin.actions.stream-actions :as admin.stream]
            [jiksnu.modules.admin.actions.subscription-actions :as admin.sub]
            [jiksnu.modules.admin.actions.user-actions :as admin.user]
            [jiksnu.modules.admin.actions.worker-actions :as admin.worker]))

(def admin-routes
  (make-matchers
   [
    [[:get    "/admin/activities.:format"]                 #'admin.activity/index]
    [[:get    "/admin/activities"]                         #'admin.activity/index]

    [[:get    "/admin/auth"]                               #'admin.auth/index]
    [[:get    "/admin/auth.:format"]                       #'admin.auth/index]

    [[:get    "/admin/clients.:format"]                    #'admin.client/index]
    [[:get    "/admin/clients"]                            #'admin.client/index]

    [[:get    "/admin/conversations.:format"]              #'admin.conversation/index]
    [[:get    "/admin/conversations"]                      #'admin.conversation/index]
    [[:post   "/admin/conversations"]                      #'admin.conversation/create]
    [[:get    "/admin/conversations/:id"]                  #'admin.conversation/show]
    [[:get    "/admin/conversations/:id.:format"]          #'admin.conversation/show]
    [[:post   "/admin/conversations/:id/update"]           #'admin.conversation/fetch-updates]
    [[:post   "/admin/conversations/:id/delete"]           #'admin.conversation/delete]

    [[:get    "/admin/feed-sources.:format"]               #'admin.feed-source/index]
    [[:get    "/admin/feed-sources"]                       #'admin.feed-source/index]
    [[:get    "/admin/feed-sources/:id.:format"]           #'admin.feed-source/show]
    [[:get    "/admin/feed-sources/:id"]                   #'admin.feed-source/show]
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

    [[:get    "/admin/group-memberships.:format"]          #'admin.group-membership/index]
    [[:get    "/admin/group-memberships"]                  #'admin.group-membership/index]

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

    [[:get    "/admin/request-tokens.:format"]             #'admin.request-token/index]
    [[:get    "/admin/request-tokens"]                     #'admin.request-token/index]

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
    [[:get    "/admin/users/:id"]                          #'admin.user/show]

    [[:get    "/admin/settings"]                           #'admin.setting/edit-page]
    [[:post   "/admin/settings"]                           #'admin.setting/update-settings]

    [[:get    "/admin/streams.:format"]                    #'admin.stream/index]
    [[:get    "/admin/streams"]                            #'admin.stream/index]

    [[:get    "/admin/workers.:format"]                    #'admin.worker/index]
    [[:get    "/admin/workers"]                            #'admin.worker/index]
    [[:post   "/admin/workers/start"]                      #'admin.worker/start-worker]
    [[:post   "/admin/workers/stop"]                       #'admin.worker/stop-worker]
    [[:post   "/admin/workers/stop/all"]                   #'admin.worker/stop-all-workers]
    ]))

