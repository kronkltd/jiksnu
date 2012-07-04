(ns jiksnu.routes
  (:use [ciste.routes :only [make-matchers resolve-routes]]
        [ring.middleware.flash :only [wrap-flash]]
        [slingshot.slingshot :only [throw+]])
  (:require [aleph.http :as http]
            ciste.formats.default
            [ciste.middleware :as middleware]
            [ciste.predicates :as pred]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [compojure.core :as compojure]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [jiksnu.actions.activity-actions :as activity]
            [jiksnu.actions.admin.activity-actions :as admin.activity]
            [jiksnu.actions.admin.auth-actions :as admin.auth]
            [jiksnu.actions.admin.conversation-actions :as admin.conversation]
            [jiksnu.actions.admin.group-actions :as admin.group]
            [jiksnu.actions.admin.feed-source-actions :as admin.feed-source]
            [jiksnu.actions.admin.feed-subscription-actions :as admin.feed-subscription]
            [jiksnu.actions.admin.like-actions :as admin.like]
            [jiksnu.actions.admin.key-actions :as admin.key]
            [jiksnu.actions.admin.subscription-actions :as admin.sub]
            [jiksnu.actions.admin.user-actions :as admin.user]
            [jiksnu.actions.admin-actions :as admin]
            [jiksnu.actions.auth-actions :as auth]
            [jiksnu.actions.comment-actions :as comment]
            [jiksnu.actions.conversation-actions :as conversation]
            [jiksnu.actions.domain-actions :as domain]
            [jiksnu.actions.favorite-actions :as favorite]
            [jiksnu.actions.feed-source-actions :as feed-source]
            [jiksnu.actions.group-actions :as group]
            [jiksnu.actions.inbox-actions :as inbox]
            [jiksnu.actions.like-actions :as like]
            [jiksnu.actions.pubsub-actions :as pubsub]
            [jiksnu.actions.message-actions :as message]
            [jiksnu.actions.salmon-actions :as salmon]
            [jiksnu.actions.search-actions :as search]
            [jiksnu.actions.setting-actions :as setting]
            [jiksnu.actions.site-actions :as site]
            [jiksnu.actions.stream-actions :as stream]
            [jiksnu.actions.subscription-actions :as sub]
            [jiksnu.actions.tag-actions :as tag]
            [jiksnu.actions.user-actions :as user]
            [jiksnu.middleware :as jm]
            [jiksnu.namespace :as ns]
            jiksnu.sections.layout-sections
            [jiksnu.session :as session]
            [jiksnu.views :as views]
            [ring.middleware.file :as file]
            [ring.middleware.file-info :as file-info]
            [ring.middleware.stacktrace :as stacktrace]
            [ring.util.response :as response])
  (:import javax.security.auth.login.LoginException))

(defn not-found-msg
  []
  "Not Found")

(defn escape-route
  [path]
  (string/replace path #":" "\\:"))

(def admin-routes
  (make-matchers
   [
    [[:get  "/admin"]                                   #'admin/index]
    [[:get  "/admin/activities"]                        #'admin.activity/index]
    [[:get  "/admin/auth"]                              #'admin.auth/index]
    [[:get  "/admin/conversations"]                     #'admin.conversation/index]
    [[:get  "/admin/conversations.:format"]             #'admin.conversation/index]
    [[:post "/admin/conversations"]                     #'admin.conversation/create]
    [[:get  "/admin/conversations/:id"]                 #'admin.conversation/show]
    [[:get  "/admin/conversations/:id.:format"]         #'admin.conversation/show]
    [[:post "/admin/conversations/:id/update"]          #'admin.conversation/fetch-updates]
    [[:post "/admin/conversations/:id/delete"]          #'admin.conversation/delete]

    [[:get  "/admin/groups"]                            #'admin.group/index]
    [[:get  "/admin/groups.:format"]                    #'admin.group/index]
    [[:post "/admin/groups"]                            #'admin.group/create]
    [[:get  "/admin/groups/:id"]                        #'admin.group/show]
    [[:get  "/admin/groups/:id.:format"]                #'admin.group/show]
    [[:post "/admin/groups/:id/delete"]                 #'admin.group/delete]


    
    
    [[:get  "/admin/keys"]                              #'admin.key/index]
    [[:get  "/admin/keys.:format"]                      #'admin.key/index]
    [[:post "/admin/keys"]                              #'admin.key/create]
    [[:get  "/admin/keys/:id"]                          #'admin.key/show]
    [[:get  "/admin/keys/:id.:format"]                  #'admin.key/show]
    [[:post "/admin/keys/:id/delete"]                   #'admin.key/delete]



    
    [[:get    "/admin/subscriptions"]                     #'admin.sub/index]
    [[:post   "/admin/subscriptions"]                     #'admin.sub/create]
    [[:get    "/admin/subscriptions/:id.:format"]         #'admin.sub/show]
    [[:get    "/admin/subscriptions/:id"]                 #'admin.sub/show]
    [[:post   "/admin/subscriptions/:id/delete"]          #'admin.sub/delete]
    [[:post   "/admin/subscriptions/:id/update"]          #'admin.sub/update]
    [[:get    "/admin/feed-sources"]                      #'admin.feed-source/index]
    [[:get    "/admin/feed-sources/:id"]                  #'admin.feed-source/show]
    [[:post   "/admin/feed-sources/:id/delete"]           #'admin.feed-source/delete]
    [[:post   "/admin/feed-sources/:id/unsubscribe"]      #'feed-source/remove-subscription]
    [[:post   "/admin/feed-sources/:id/update"]           #'feed-source/fetch-updates]
    [[:post   "/admin/feed-sources/:id/watchers"]         #'feed-source/add-watcher]
    [[:post   "/admin/feed-sources/:id/watchers/delete"]  #'feed-source/remove-watcher]
    [[:get    "/admin/feed-subscriptions"]                #'admin.feed-subscription/index]
    [[:get    "/admin/likes"]                             #'admin.like/index]
    [[:delete "/admin/likes/:id"]                  #'admin.like/delete]
    [[:post   "/admin/likes/:id/delete"]                  #'admin.like/delete]
    [[:post   "/admin/users"]                             #'admin.user/create]
    [[:get    "/admin/users"]                             #'admin.user/index]
    [[:get    "/admin/settings"]                          #'setting/admin-edit-page]
    [[:post   "/admin/settings"]                          #'setting/update-settings]]))

(def http-routes
  (make-matchers
   [
    [[:get    "/"]                                            #'stream/public-timeline]
    [[:get    "/.well-known/host-meta"]                       #'domain/host-meta]
    [[:get    "/.well-known/host-meta.:format"]               #'domain/host-meta]
    [[:get    "/api/account/verify_credentials.:format"]      #'auth/verify-credentials]
    [[:get    "/api/direct_messages.:format"]                 #'stream/direct-message-timeline]
    ;; FIXME: identicurse sends a post. seems wrong to me.
    [[:post   "/api/direct_messages.:format"]                 #'stream/direct-message-timeline]
    [[:get    "/api/friendships/exists.:format"]              #'user/exists?]
    [[:get    "/api/people/@me/@all"]                         #'user/index]
    [[:get    "/api/people/@me/@all/:id"]                     #'user/show]
    [[:get    "/api/statuses/friends_timeline.:format"]       #'stream/home-timeline]
    [[:get    "/api/statuses/home_timeline.:format"]          #'stream/home-timeline]
    [[:get    "/api/statuses/mentions.:format"]               #'stream/mentions-timeline]
    [[:get    "/api/statusnet/app/favorites/:id.:format"]     #'favorite/user-list]
    [[:get    "/api/statusnet/app/memberships/:id.:format"]   #'group/user-list]
    [[:get    "/api/statusnet/app/service.:format"]           #'site/service]
    [[:get    "/api/statusnet/app/subscriptions/:id.:format"] #'sub/get-subscriptions]
    [[:get    "/api/statusnet/config.:format"]                #'setting/config-output]
    [[:post   "/api/statuses/update.:format"]                 #'activity/post]
    ;; [[:get    "/api/mentions"]                                 #'stream/mentions-timeline]
    [[:get    "/api/statuses/public_timeline.:format"]        #'stream/public-timeline]
    [[:get    "/api/statuses/show/:id.:format"]               #'activity/show]
    [[:get    "/api/statuses/user_timeline/:id.:format"]      #'stream/user-timeline]
    [[:get    "/groups"]                                      #'group/index]
    [[:post   "/groups"]                                      #'group/create]
    [[:get    "/groups/new"]                                  #'group/new-page]
    [[:get    "/groups/:name"]                                #'stream/group-timeline]
    [[:get    "/groups/:name/edit"]                           #'group/edit-page]
    [[:get    "/main/conversations"]                          #'conversation/index]
    [[:get    "/main/conversations.:format"]                  #'conversation/index]
    [[:get    "/main/conversations/:id"]                      #'conversation/show]
    [[:get    "/main/conversations/:id.:format"]              #'conversation/show]
    [[:get    "/main/domains.:format"]                        #'domain/index]
    [[:get    "/main/domains"]                                #'domain/index]
    [[:get    "/main/domains/:id.:format"]                    #'domain/show]
    [[:get    "/main/domains/:id"]                            #'domain/show]
    [[:delete "/main/domains/*"]                              #'domain/delete]
    [[:post   "/main/domains/:id/discover"]                   #'domain/discover]
    [[:post   "/main/domains/:id/edit"]                       #'domain/edit-page]
    [[:post   "/main/domains"]                                #'domain/find-or-create]
    [[:post   "/main/guest-login"]                            #'auth/guest-login]
    [[:post   "/likes/:id/delete"]                            #'like/delete]
    [[:get    "/main/login"]                                  #'auth/login-page]
    [[:post   "/main/login"]                                  #'auth/login]
    [[:get    "/main/logout"]                                 #'auth/logout]
    [[:post   "/main/logout"]                                 #'auth/logout]
    ;; [[:get "/main/events"]                                     #'activity/stream]
    [[:get    "/main/oembed"]                                 #'activity/oembed]
    [[:get    "/main/ostatus"]                                #'sub/ostatus]
    [[:get    "/main/ostatussub"]                             #'sub/ostatussub]
    [[:post   "/main/ostatussub"]                             #'sub/ostatussub-submit]
    [[:get    "/main/password"]                               #'auth/password-page]
    [[:get    "/main/push/hub"]                               #'pubsub/hub-dispatch]
    [[:post   "/main/push/hub"]                               #'pubsub/hub-dispatch]
    [[:get    "/main/push/callback"]                          #'feed-source/process-updates]
    [[:post   "/main/push/callback"]                          #'stream/callback-publish]
    [[:get    "/main/register"]                               #'user/register-page]
    [[:post   "/main/register"]                               #'user/register]
    [[:post   "/main/salmon/user/:id"]                        #'salmon/process]
    [[:get    "/main/search"]                                 #'search/perform-search]
    [[:post   "/main/search"]                                 #'search/perform-search]
    [[:post   "/main/subscribe"]                              #'sub/subscribe]
    [[:post   "/main/unsubscribe"]                            #'sub/unsubscribe]
    [[:get    "/main/xrd"]                                    #'user/user-meta]
    [[:get    "/notice/:id.:format"]                          #'activity/show]
    [[:get    "/notice/:id"]                                  #'activity/show]
    [[:get    "/notice/:id/comment"]                          #'comment/new-comment]
    [[:post   "/notice/:id/comments"]                         #'comment/add-comment]
    [[:post   "/notice/:id/comments/update"]                  #'comment/fetch-comments]
    [[:get    "/notice/:id/edit"]                             #'activity/edit-page]
    [[:post   "/notice/:id/like"]                             #'like/like-activity]
    [[:post   "/notice/new"]                                  #'activity/post]
    [[:post   "/notice/:id"]                                  #'activity/update]
    [[:delete "/notice/:id"]                                  #'activity/delete]
    [[:get    "/opensearch/people"]                           #'search/os-people]
    [[:get    "/opensearch/notices"]                          #'search/os-notice]
    [[:get    "/rsd.xml"]                                     #'site/rsd]
    [[:get    "/remote-user/*"]                               #'stream/remote-user]
    ;; [[:get    "/search/group"]                                 #'group/search-page]
    ;; [[:post   "/search/group"]                                 #'group/search]
    [[:get    "/settings/avatar"]                             #'setting/avatar-page]
    [[:get    "/main/profile"]                                #'user/profile]
    [[:post   "/main/profile"]                                #'user/update-profile]
    [[:get    "/settings/oauthapps"]                          #'setting/oauth-apps]
    [[:delete "/subscriptions/:id"]                           #'sub/delete]
    [[:get    "/tags/:name.:format"]                          #'tag/show]
    [[:get    "/tags/:name"]                                  #'tag/show]
    ;; [[:get    "/tags"]                                        #'tag/index]
    [[:get    "/users.:format"]                               #'user/index]
    [[:get    "/users"]                                       #'user/index]
    [[:get    "/users/local.:format"]                         #'user/local-index]
    [[:get    "/users/local"]                                 #'user/local-index]
    [[:get    "/users/:id.:format"]                           #'stream/remote-profile]
    [[:delete "/users/:id"]                                   #'user/delete]
    [[:get    "/users/:id"]                                   #'stream/remote-profile]
    [[:post   "/users/:id/delete"]                            #'user/delete]
    [[:post   "/users/:id/discover.:format"]                  #'user/discover]
    [[:post   "/users/:id/discover"]                          #'user/discover]
    [[:post   "/users/:subscribeto/subscribe.:format"]        #'sub/subscribe]
    [[:post   "/users/:subscribeto/subscribe"]                #'sub/subscribe]
    [[:get    "/users/:id/subscriptions.:format"]             #'sub/get-subscriptions]
    [[:get    "/users/:id/subscriptions"]                     #'sub/get-subscriptions]
    [[:get    "/users/:id/subscribers.:format"]               #'sub/get-subscribers]
    [[:get    "/users/:id/subscribers"]                       #'sub/get-subscribers]
    [[:post   "/users/:id/unsubscribe"]                       #'sub/unsubscribe]
    [[:post   "/users/:id/update.:format"]                    #'user/fetch-updates]
    [[:post   "/users/:id/update"]                            #'user/fetch-updates]
    [[:post   "/users/:id/update-hub"]                        #'user/update-hub]
    ;; [[:post   "/users/:id/push/subscribe"]                     #'push/subscribe]
    [[:post   "/:username"]                                   #'user/update]

    [[:get    "/:username.:format"]                           #'stream/user-timeline]
    [[:get    "/:username"]                                   #'stream/user-timeline]
    [[:get    "/:username/all"]                               #'stream/home-timeline]
    [[:get    "/:username/inbox"]                             #'message/inbox-page]
    [[:get    "/:username/groups"]                            #'group/user-list]
    [[:get    "/:username/microsummary"]                      #'stream/user-microsummary]
    [[:get    "/:username/outbox"]                            #'message/outbox-page]
    [[:get    "/:username/streams"]                           #'stream/user-list]
    [[:post   "/:username/streams"]                           #'stream/add]
    [[:get    "/:username/streams/new"]                       #'stream/add-stream-page]
    [[:get    "/:username/subscribers.:format"]               #'sub/get-subscribers]
    [[:get    "/:username/subscribers"]                       #'sub/get-subscribers]
    [[:get    "/:username/subscriptions.:format"]             #'sub/get-subscriptions]
    [[:get    "/:username/subscriptions"]                     #'sub/get-subscriptions]
    ]))

(def xmpp-routes
  (map
   (fn [[m a]]
     [(merge {:serialization :xmpp
              :format :xmpp} m)
      {:action a}])
   [[{:method :get
      :pubsub true
      :name "items"
      :node (escape-route ns/microblog)}
     #'stream/user-timeline]

    [{:method :set
      :pubsub true
      :name "publish"
      :node (escape-route ns/microblog)}
     #'activity/post]

    [{:method :get
      :pubsub true
      :name "items"
      :node (str ns/microblog ":replies:item=:id")}
     #'comment/fetch-comments]

    [{:method :error
      :name "ping"}
     #'domain/ping-error]

    [{:method :get
      :name "query"
      :ns ns/vcard-query}
     #'user/show]

    [{:method :set
      :name "publish"
      :ns ns/vcard}
     #'user/create]

    ;; [{:method :result
    ;;   :name "query"
    ;;   :ns ns/vcard-query}
    ;;  #'user/remote-create]

    [{:method :error
      :name "error"}
     #'user/xmpp-service-unavailable]

    [{:method :result
      :pubsub true
      :node (str ns/microblog ":replies:item=:id")
      :ns ns/pubsub}
     #'comment/comment-response]

    [{:method :get
      :name "subscriptions"}
     #'sub/get-subscriptions]

    [{:method :set
      :name "subscribe"
      :ns ns/pubsub}
     #'sub/subscribed]

    [{:method :set
      :name "unsubscribe"
      :ns ns/pubsub}
     #'sub/unsubscribe]

    [{:method :get
      :name "subscribers"}
     #'sub/get-subscribers]

    [{:method :set
      :name "subscribers"}
     #'sub/subscribed]

    [{:method :result
      :name "subscription"
      :ns ns/pubsub}
     #'sub/remote-subscribe-confirm]

    ;; FIXME: This is way too general
    ;; [{:method :headline}
    ;;  #'activity/remote-create]

    ;; [{:method :result
    ;;   :pubsub true
    ;;   :node ns/microblog}
    ;;  #'activity/remote-create]

    [{:method :get
      :pubsub true
      :node (escape-route ns/inbox)}
     #'inbox/index]

    #_[{:method :result
        :pubsub false} #'domain/ping-response]]))

(def http-predicates
  [#'pred/request-method-matches?
   #'pred/path-matches?])

(def xmpp-predicates
  [#'pred/type-matches?
   #'pred/node-matches?
   #'pred/name-matches?
   #'pred/ns-matches?])

(compojure/defroutes all-routes
  (compojure/GET "/api/help/test.json" _ "OK")
  (jm/wrap-authentication-handler
   (compojure/ANY "/admin*" request
                  (if (session/is-admin?)
                    ((resolve-routes [http-predicates] admin-routes) request)
                    ;; TODO: move this somewhere else
                    (throw+ {:type :authentication :message "Must be admin"}))))
  (middleware/wrap-log-request
   (resolve-routes [http-predicates] http-routes))
  (compojure/GET "/websocket" _
                 (http/wrap-aleph-handler stream/websocket-handler))
  (compojure/GET "/main/events" _
                 stream/stream-handler)
  (route/not-found (not-found-msg)))

(def app
  (http/wrap-ring-handler
   (-> all-routes
       jm/wrap-authentication-handler
       (file/wrap-file "resources/public/")
       file-info/wrap-file-info
       jm/wrap-user-binding
       middleware/wrap-http-serialization
       #_middleware/wrap-log-request
       handler/site
       jm/wrap-stacktrace)))
