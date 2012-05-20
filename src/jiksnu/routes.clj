(ns jiksnu.routes
  (:use [ciste.debug :only [spy]]
        [ciste.routes :only [make-matchers resolve-routes]]
        [ring.middleware.flash :only [wrap-flash]])
  (:require [aleph.http :as http]
            ciste.formats.default
            [ciste.middleware :as middleware]
            [ciste.predicates :as pred]
            [clojure.string :as string]
            [compojure.core :as compojure]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [jiksnu.actions.activity-actions :as activity]
            [jiksnu.actions.admin.activity-actions :as admin.activity]
            [jiksnu.actions.admin.auth-actions :as admin.auth]
            [jiksnu.actions.admin.feed-source-actions :as admin.feed-source]
            [jiksnu.actions.admin.subscription-actions :as admin.sub]
            [jiksnu.actions.admin.user-actions :as admin.user]
            [jiksnu.actions.admin-actions :as admin]
            [jiksnu.actions.auth-actions :as auth]
            [jiksnu.actions.comment-actions :as comment]
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
            [jiksnu.namespace :as namespace]
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
    [[:get  "/admin/subscriptions"]                     #'admin.sub/index]
    [[:get  "/admin/feed-sources"]                      #'admin.feed-source/index]
    [[:get  "/admin/feed-sources/:id"]                  #'admin.feed-source/show]
    [[:post  "/admin/feed-sources/:id/unsubscribe"]      #'feed-source/remove-subscription]
    ;; [[:get  "/admin/feed-subscribers"]                  #'admin.feed-subscriber/index]
    [[:post "/admin/users"]                             #'admin.user/create]
    [[:get  "/admin/users"]                             #'admin.user/index]
    [[:get  "/admin/settings"]                          #'setting/admin-edit-page]
    [[:post "/admin/settings"]                          #'setting/update-settings]]))

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
    [[:post   "/groups"]                                       #'group/add]
    [[:get    "/groups/new"]                                  #'group/new-page]
    [[:get    "/groups/:name"]                                #'stream/group-timeline]
    [[:get    "/main/domains"]                                #'domain/index]
    [[:get    "/main/domains/:id.:format"]                    #'domain/show]
    [[:get    "/main/domains/:id"]                            #'domain/show]
    [[:delete "/main/domains/*"]                              #'domain/delete]
    [[:post   "/main/domains/:id/discover"]                   #'domain/discover]
    [[:post   "/main/domains/:id/edit"]                       #'domain/edit-page]
    [[:post   "/main/domains"]                                #'domain/find-or-create]
    [[:post   "/main/guest-login"]                            #'auth/guest-login]
    [[:get    "/main/login"]                                  #'auth/login-page]
    [[:post   "/main/login"]                                  #'auth/login]
    [[:get    "/main/logout"]                                 #'auth/logout]
    [[:post   "/main/logout"]                                 #'auth/logout]
    ;; [[:get "/main/events"]                                     #'activity/stream]
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
    [[:get    "/tags"]                                        #'tag/index]
    [[:get    "/users.:format"]                               #'user/index]
    [[:get    "/users"]                                       #'user/index]
    [[:get    "/users/local.:format"]                         #'user/local-index]
    [[:get    "/users/local"]                                 #'user/local-index]
    [[:get    "/users/:id.:format"]                           #'stream/remote-profile]
    [[:delete "/users/:id"]                                   #'user/delete]
    [[:get    "/users/:id"]                                   #'stream/remote-profile]
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
      :node (escape-route namespace/microblog)}
     #'stream/user-timeline]

    [{:method :set
      :pubsub true
      :name "publish"
      :node (escape-route namespace/microblog)}
     #'activity/post]

    [{:method :get
      :pubsub true
      :name "items"
      :node (str namespace/microblog ":replies:item=:id")}
     #'comment/fetch-comments]

    [{:method :error
      :name "ping"}
     #'domain/ping-error]

    [{:method :get
      :name "query"
      :ns namespace/vcard-query}
     #'user/show]

    [{:method :set
      :name "publish"
      :ns namespace/vcard}
     #'user/create]

    [{:method :result
      :name "query"
      :ns namespace/vcard-query}
     #'user/remote-create]

    [{:method :error
      :name "error"}
     #'user/xmpp-service-unavailable]

    [{:method :result
      :pubsub true
      :node (str namespace/microblog ":replies:item=:id")
      :ns namespace/pubsub}
     #'comment/comment-response]

    [{:method :get
      :name "subscriptions"}
     #'sub/get-subscriptions]

    [{:method :set
      :name "subscribe"
      :ns namespace/pubsub}
     #'sub/subscribed]

    [{:method :set
      :name "unsubscribe"
      :ns namespace/pubsub}
     #'sub/unsubscribe]

    [{:method :get
      :name "subscribers"}
     #'sub/get-subscribers]

    [{:method :set
      :name "subscribers"}
     #'sub/subscribed]

    [{:method :result
      :name "subscription"
      :ns namespace/pubsub}
     #'sub/remote-subscribe-confirm]

    ;; FIXME: This is way too general
    [{:method :headline}
     #'activity/remote-create]

    [{:method :result
      :pubsub true
      :node namespace/microblog}
     #'activity/remote-create]

    [{:method :get
      :pubsub true
      :node (escape-route namespace/inbox)}
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
  (jm/wrap-authentication-handler
   (compojure/ANY "/admin*" request
                  (if (session/is-admin?)
                    ((resolve-routes [http-predicates] admin-routes) request)
                    ;; TODO: move this somewhere else
                    (throw (LoginException. "Must be admin")))))
  (middleware/wrap-log-request
   (resolve-routes [http-predicates] http-routes))
  (compojure/GET "/websocket" _
                 (http/wrap-aleph-handler stream/websocket-handler))
  (compojure/GET "/main/events" _
                 (http/wrap-aleph-handler stream/stream-handler))
  (route/not-found (not-found-msg)))

(def app
  (http/wrap-ring-handler
   
   (-> all-routes
       jm/wrap-authentication-handler
       (file/wrap-file "resources/public/")
       file-info/wrap-file-info
       wrap-flash
       jm/wrap-user-binding
       handler/site
       middleware/wrap-http-serialization
       #_middleware/wrap-log-request
       #_stacktrace/wrap-stacktrace)))

