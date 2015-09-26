(ns jiksnu.modules.web.routes.access-token-routes
  (:require [jiksnu.actions.access-token-actions :as actions.access-token]

[ciste.commands :refer [add-command!]]
            ;; [ciste.routes :refer [make-matchers]]
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
            [jiksnu.modules.admin.actions.worker-actions :as admin.worker]
            [jiksnu.actions.comment-actions :as comment]
            [jiksnu.actions :as actions]
[jiksnu.modules.web.actions.core-actions :as actions.web.core]
[jiksnu.actions.dialback-actions :as dialback]
))



(defn routes
  []
  [
   ;; [[:get  "/"]                                       #'actions.stream/public-timeline]

   [[:get  "/api/direct_messages.:format"]            #'actions.stream/direct-message-timeline]
   ;; FIXME: identicurse sends a post. seems wrong to me.
   [[:post "/api/direct_messages.:format"]            #'actions.stream/direct-message-timeline]

   [[:get  "/api/statuses/friends_timeline.:format"]  #'actions.stream/home-timeline]
   [[:get  "/api/statuses/home_timeline.:format"]     #'actions.stream/home-timeline]
   [[:get  "/api/statuses/mentions"]                  #'actions.stream/mentions-timeline]
   ;; [[:get    "/api/mentions"]                                     #'actions.stream/mentions-timeline]
   [[:get  "/api/statuses/public_timeline.:format"]   #'actions.stream/public-timeline]
   [[:get  "/api/statuses/user_timeline/:id"]         #'actions.stream/user-timeline]

   [[:get  "/api/user/:username/feed"]                {:action #'actions.stream/user-timeline      :format :as}]
   [[:post "/api/user/:username/feed"]                {:action #'actions.activity/post             :format :as}]
   [[:get  "/api/user/:username/inbox/direct/major"]  {:action #'actions.stream/direct-inbox-major :format :as}]
   [[:get  "/api/user/:username/inbox/direct/minor"]  {:action #'actions.stream/direct-inbox-minor :format :as}]
   [[:get  "/api/user/:username/inbox/major"]         {:action #'actions.stream/inbox-major        :format :as}]
   [[:get  "/api/user/:username/inbox/minor"]         {:action #'actions.stream/inbox-minor        :format :as}]

   ;; [[:post "/main/push/callback"]                     #'actions.stream/callback-publish]

   ;; [[:get  "/remote-user/*"]                          #'actions.stream/user-timeline]
   [[:post "/streams"]                                #'actions.stream/create]

   ;; [[:get  "/users/:id.:format"]                      #'actions.stream/user-timeline]
   ;; [[:get  "/users/:id"]                              #'actions.stream/user-timeline]

   ;; [[:get  "/:username.:format"]                      #'actions.stream/user-timeline]
   ;; [[:get  "/:username"]                              #'actions.stream/user-timeline]
   ;; [[:get  "/:username/all"]                          #'actions.stream/home-timeline]
   ;; [[:get  "/:username/streams"]                      #'actions.stream/user-list]
   ;; [[:post "/:username/streams"]                      #'actions.stream/add]
   ;; [[:get  "/:username/microsummary"]                 #'actions.stream/user-microsummary]
   ;; [[:get  "/:username/streams/new"]                  #'actions.stream/add-stream-page]
   [[:get    "/api/statusnet/app/subscriptions/:id.:format"] #'sub/get-subscriptions]
   [[:get    "/main/ostatus"]                                #'sub/ostatus]
   [[:get    "/main/ostatussub"]                             #'sub/ostatussub]
   [[:post   "/main/ostatussub"]                             #'sub/ostatussub-submit]
   [[:post   "/main/subscribe"]                              #'sub/subscribe]
   [[:post   "/main/unsubscribe"]                            #'sub/unsubscribe]
   [[:delete "/subscriptions/:id"]                           #'sub/delete]
   [[:get    "/users/:id/subscriptions.:format"]             #'sub/get-subscriptions]
   [[:get    "/users/:id/subscriptions"]              #'sub/get-subscriptions]
   [[:get    "/users/:id/subscribers.:format"]               #'sub/get-subscribers]
   [[:get    "/users/:id/subscribers"]                #'sub/get-subscribers]
   [[:post   "/users/:id/unsubscribe"]                       #'sub/unsubscribe]
   [[:get    "/:username/subscribers.:format"]               #'sub/get-subscribers]
   [[:get    "/:username/subscribers"]                       #'sub/get-subscribers]
   [[:get    "/:username/subscriptions.:format"]             #'sub/get-subscriptions]
   [[:get    "/:username/subscriptions"]                     #'sub/get-subscriptions]
   [[:post   "/users/:subscribeto/subscribe.:format"]        #'sub/subscribe]
   [[:post   "/users/:subscribeto/subscribe"]                #'sub/subscribe]
   [[:get    "/model/subscriptions/:id"]          #'sub/show]

   [[:get "/api/user/:username/following"]                 {:action #'sub/get-subscriptions
                                                            :format :as
                                                            }]
   [[:get "/api/user/:username/followers"]                 {:action #'sub/get-subscribers
                                                            :format :as
                                                            }]
   [[:get    "/tags/:name.:format"] #'tag/show]
   [[:get    "/tags/:name"]         #'tag/show]
   ;; [[:get    "/tags"]               #'tag/index]
   ;; [[:get    "/api/friendships/exists.:format"] #'user/exists?]

   ;; [[:get    "/api/people/@me/@all"]            #'user/index]

   ;; [[:get    "/api/people/@me/@all/:id"]        #'user/show]
   ;; [[:get    "/main/profile"]                   #'user/profile]
   ;; [[:get    "/main/xrd"]                       #'user/user-meta]
   ;; [[:get    "/model/users/:id"]                #'user/show]
   ;; [[:get    "/users"]                          #'user/index]
   ;; [[:post   "/users/:id/update-hub"]           #'user/update-hub]
   ;; [[:post   "/:username"]                      #'user/update-record]


   [[:get    "/api/user/:username/"]            {:action #'user/show-basic
                                                 :format :as}]
   [[:get    "/api/user/:username/profile"]     {:action #'user/show
                                                 :format :as}]

   [[:post   "/main/profile"]                   #'user/update-profile]
   [[:post   "/main/register"]                  #'user/register]
   [[:get    "/users.:format"]                  #'user/index]
   [[:get    "/users/:id"]                      #'user/show]
   [[:get    "/users/:id.:format"]              #'user/show]
   [[:get    "/users/:user@:domain.:format"]    #'user/show]
   [[:delete "/users/:id"]                      #'user/delete]
   [[:post   "/users/:id/discover.:format"]     #'user/discover]
   [[:post   "/users/:id/discover"]             #'user/discover]
   [[:post   "/users/:id/update.:format"]       #'user/update-record]
   [[:post   "/users/:id/update"]               #'user/update-record]
   [[:post   "/users/:id/streams"]              #'user/add-stream]
   [[:post   "/users/:id/delete"]               #'user/delete]
   [[:get "/api/statusnet/app/service"] #'site/service]
   [[:get "/rsd.:format"]               #'site/rsd]
   [[:get "/rsd"]                       {:action #'site/rsd :format :xml}]
   [[:get "/status"]                    {:action #'site/status :format :json}]
   [[:get "/main/stats.:format"]        #'site/get-stats]
   [[:get "/main/stats"]                #'site/get-stats]
   [[:get "/api/statusnet/config.:format"] #'setting/config-output]
   [[:get "/settings/avatar"]              #'setting/avatar-page]
   [[:get "/settings/oauthapps"]           #'setting/oauth-apps]
   [[:get  "/main/search"] #'search/perform-search]
   [[:post "/main/search"] #'search/perform-search]
   [[:get  "/opensearch/people"]   #'search/os-people]
   [[:get  "/opensearch/notices"]  #'search/os-notice]
   [[:get    "/resources.:format"]   #'index]
   ;; [[:get    "/resources"]   #'index]
   [[:get    "/resources/:id.:format"]     #'show]
   ;; [[:get    "/resources/:id"]     #'show]
   [[:post   "/resources/:id/discover.:format"] #'discover]
   [[:post   "/resources/:id/discover"] #'discover]
   [[:post   "/resources/:id/update.:format"]   #'update-record]
   [[:post   "/resources/:id/update"]   #'update-record]
   [[:delete "/resources/:id"]     #'delete]
   [[:post   "/resources/:id/delete"]   #'delete]
   [[:get    "/model/resources/:id"]    #'show]
   [[:post "/oauth/request_token"]      {:action #'actions.request-token/get-request-token
                                         :format :text}]

   [[:post "/oauth/authorize"]          {:action #'actions.request-token/authorize
                                         :format :html
                                         }]

   [[:get "/oauth/authorize"]           {:action #'actions.request-token/show-authorization-form
                                         :format :html}]

   [[:get  "/main/push/hub"] #'pubsub/hub-dispatch]
   [[:post "/main/push/hub"] #'pubsub/hub-dispatch]
   ;; [[:post   "/users/:id/push/subscribe"] #'pubsub/subscribe]
   [[:get "/oauth/request_token"] #'actions.oauth/request-token]
   [[:get "/oauth/authorize"]     #'actions.oauth/authorize]
   [[:get "/oauth/access_token"]  #'actions.oauth/access-token]
   [[:get "/:username/inbox"]  #'message/inbox-page]
   [[:get "/:username/outbox"] #'message/outbox-page]
   [[:post   "/likes/:id/delete"] #'like/delete]
   [[:post   "/notice/:id/like"]  #'like/like-activity]
   [[:get  "/api/statusnet/app/memberships/:id.:format"] #'group/user-list]

   [[:get  "/main/groups.:format"]                       #'group/index]
   ;; [[:get  "/main/groups"]                               #'group/index]
   [[:post "/main/groups"]                               #'group/create]
   [[:get  "/main/groups/new"]                           #'group/new-page]
   [[:get  "/main/groups/:name.:format"]                 #'group/show]
   ;; [[:get  "/main/groups/:name"]                         #'group/show]
   ;; [[:get  "/main/groups/:name/edit"]                    #'group/edit-page]
   [[:post "/main/groups/:name/join"]                    #'group/join]

   [[:get  "/model/groups/:id.:format"]                  #'group/show]
   ;; [[:get  "/model/groups/:id"]                          #'group/show]

   [[:get  "/users/:id/groups.:format"]                  #'group/fetch-by-user]
   ;; [[:get  "/users/:id/groups"]                          #'group/fetch-by-user]
   ;; [[:get    "/search/group"]                            #'group/search-page]
   ;; [[:post   "/search/group"]                            #'group/search]

   [[:get "/main/feed-subscriptions"] #'feed-subscription/index]
   [[:get "/main/feed-subscriptions.:format"] #'feed-subscription/index]
   [[:get "/main/feed-subscriptions/:id"]   #'feed-subscription/show]
   [[:get "/main/feed-subscriptions/:id.:format"]   #'feed-subscription/show]
   [[:get "/model/feedSubscriptions/:id"]  #'feed-subscription/show]
   [[:get "/main/feed-sources"]  #'feed-source/index]
   [[:get "/main/feed-sources.:format"]  #'feed-source/index]
   [[:get "/main/feed-sources/:id.:format"]  #'feed-source/show]
   [[:get "/main/feed-sources/:id"]  #'feed-source/show]
   [[:get "/main/push/callback"]     #'feed-source/process-updates]
   [[:get "/model/feed-sources/:id"] #'feed-source/show]
   [[:get    "/api/statusnet/app/favorites/:id.:format"]     #'favorite/user-list]
   [[:get "/api/user/:username/favorites"]  {:action #'favorite/user-list
                                             :format :as
                                             }]
   ;; [[:get    "/.well-known/host-meta.:format"]   #'actions.domain/show]
   ;; [[:get    "/.well-known/host-meta"]           {:action #'actions.domain/show
   ;;                                                :format :xrd}]
   ;; [[:get    "/main/domains.:format"]            #'actions.domain/index]
   ;; [[:get    "/main/domains"]                    #'actions.domain/index]
   ;; [[:get    "/main/domains/:id.:format"]        #'actions.domain/show]
   ;; [[:get    "/main/domains/:id"]                #'actions.domain/show]
   ;; [[:delete "/main/domains/*"]                  #'actions.domain/delete]
   ;; [[:post   "/main/domains/:id/edit"]           #'actions.domain/edit-page]
   ;; [[:post   "/main/domains"]                    #'actions.domain/find-or-create]
   ;; [[:get    "/api/dialback"]                    #'actions.domain/dialback]
   [[:post "/api/dialback"]  #'dialback/confirm]
   ;; [[:get "/main/conversations.:format"] #'conversation/index]
   ;; [[:get "/main/conversations"] #'conversation/index]
   ;; [[:get "/main/conversations/:id.:format"]  #'conversation/show]
   ;; [[:get "/main/conversations/:id"]  #'conversation/show]
   ;; [[:get "/model/conversations/:id"] #'conversation/show]
   [[:get  "/main/confirm"]          #'actions/confirm]
   [[:get "/nav.js"] {:action #'actions.web.core/nav-info :format :json}]

   [[:get    "/notice/:id/comment"]                          #'comment/new-comment]
   [[:post   "/notice/:id/comments"]                         #'comment/add-comment]
   [[:post   "/notice/:id/comments/update"]                  #'comment/fetch-comments]



   [[[:post "/api/client/register"]           {:action #'actions.client/register
                                              :format :json}]]


   [[:post   "/api/statuses/update.:format"]   #'actions.activity/post]
   [[:get    "/api/statuses/show/:id.:format"] #'actions.activity/show]
   ;; [[:get    "/main/oembed"]                   #'actions.activity/oembed]
   [[:get    "/notice/:id.:format"]            #'actions.activity/show]
   [[:get    "/notice/:id"]                    #'actions.activity/show]
   [[:post   "/notice/new"]                    #'actions.activity/post]
   [[:post   "/notice/:id"]                    #'actions.activity/edit]
   [[:delete "/notice/:id.:format"]            #'actions.activity/delete]
   [[:delete "/notice/:id"]                    #'actions.activity/delete]
   ;; [[:get    "/notice/:id/edit"]               #'actions.activity/edit-page]
   ;; [[:get    "/model/activities/:id"]          #'actions.activity/show]
   ;; [[:get "/main/events"]                      #'actions.activity/stream]

   [[:post "/oauth/access_token"]      {:action #'actions.access-token/get-access-token
                                        :format :text}]
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

   ]
  )
