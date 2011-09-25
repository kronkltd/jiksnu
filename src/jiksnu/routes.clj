(ns jiksnu.routes
  (:use (ciste [debug :only (spy)]
               filters predicates routes)
        ciste.formats.default
        jiksnu.middleware
        lamina.core
        (ring.middleware [params :only (wrap-params)]
                         keyword-params
                         flash
                         nested-params
                         multipart-params
                         cookies
                         session))
  (:require (aleph [http :as http])
            (ciste [middleware :as middleware])
            (compojure [core :as compojure]
                       handler
                       [route :as route])
            [clojure.string :as string]
            [clojure.java.io :as io]
            (jiksnu [namespace :as namespace]
                    [view :as view])
            (jiksnu.actions [activity-actions :as activity]
                            [auth-actions :as auth]
                            [comment-actions :as comment]
                            [domain-actions :as domain]
                            [inbox-actions :as inbox]
                            [like-actions :as like]
                            [push-subscription-actions :as push]
                            [salmon-actions :as salmon]
                            [settings-actions :as settings]
                            [stream-actions :as stream]
                            [subscription-actions :as subscription]
                            [user-actions :as user]
                            [webfinger-actions :as webfinger])
            (jiksnu.filters activity-filters
                            auth-filters
                            domain-filters
                            push-subscription-filters
                            salmon-filters
                            stream-filters
                            subscription-filters
                            user-filters
                            webfinger-filters)
            (jiksnu.triggers activity-triggers
                             domain-triggers
                             subscription-triggers
                             user-triggers)
            (jiksnu.views activity-views
                          auth-views
                          domain-views
                          push-subscription-views
                          stream-views
                          subscription-views
                          user-views
                          webfinger-views)
            (ring.middleware [file :as file]
                             [file-info :as file-info]
                             [stacktrace :as stacktrace])
            [ring.util.response :as response]))

(defn not-found-msg
  []
  "You found the hidden page. Don't tell anyone about this.\n")

(defn escape-route
  [path]
  (string/replace path #":" "\\:"))

(def http-routes
  (make-matchers
   [[[:get  "/"]                                       #'stream/index]
    [[:get "/.well-known/host-meta"]                   #'webfinger/host-meta]
    [[:get "/admin/subscriptions"]                     #'subscription/index]
    [[:get "/admin/push/subscriptions"]                #'push/index]
    [[:post "/admin/users"]                            #'user/create]
    [[:get "/admin/users"]                             #'user/index]
    [[:get "/admin/settings"]                          #'settings/edit]
    [[:get "/api/people/@me/@all"]                     #'user/index]
    [[:get "/api/people/@me/@all/:id"]                 #'user/show]
    [[:get "/api/statuses/public_timeline.:format"]    #'stream/index]
    [[:get  "/api/statuses/user_timeline/:id.:format"] #'stream/user-timeline]
    [[:get "/main/domains"]                            #'domain/index]
    [[:get "/main/domains/*"]                          #'domain/show]
    [[:delete "/main/domains/*"]                       #'domain/delete]
    [[:post "/main/domains/*/discover"]                #'domain/discover]
    [[:post "/main/domains/*/edit"]                    #'domain/edit]
    [[:post "/main/domains"]                           #'domain/find-or-create]
    [[:post "/main/guest-login"]                       #'auth/guest-login]
    [[:get "/main/login"]                              #'auth/login-page]
    [[:post "/main/login"]                             #'auth/login]
    [[:post "/main/logout"]                            #'auth/logout]
    ;; [[:get "/main/events"]                             #'activity/stream]
    [[:get "/main/ostatus"]                            #'subscription/ostatus]
    [[:get "/main/ostatussub"]                         #'subscription/ostatussub]
    [[:post "/main/ostatussub"]                        #'subscription/ostatussub-submit]
    [[:get "/main/password"]                           #'auth/password-page]
    [[:get "/main/push/hub"]                           #'push/hub]
    [[:post "/main/push/hub"]                          #'push/hub-publish]
    [[:get "/main/push/callback"]                      #'push/callback]
    [[:post "/main/push/callback"]                     #'push/callback-publish]
    [[:get "/main/register"]                           #'user/register-page]
    [[:post "/main/register"]                          #'user/register]
    [[:post "/main/salmon/user/:id"]                   #'salmon/process]
    [[:post "/main/subscribe"]                         #'subscription/subscribe]
    [[:post "/main/unsubscribe"]                       #'subscription/unsubscribe]
    [[:get "/main/xrd"]                                #'webfinger/user-meta]
    [[:get "/notice/:id"]                              #'activity/show]
    [[:get "/notice/:id.:format"]                      #'activity/show]
    [[:get  "/notice/:id/comment"]                     #'comment/new-comment]
    [[:post "/notice/:id/comments"]                    #'comment/add-comment]
    [[:post "/notice/:id/comments/update"]             #'comment/fetch-comments]
    [[:get "/notice/:id/edit"]                         #'activity/edit]
    [[:post "/notice/:id/likes"]                       #'like/like-activity]
    [[:post "/notice/new"]                             #'activity/post]
    [[:post "/notice/:id"]                             #'activity/update]
    [[:delete "/notice/:id"]                           #'activity/delete]
    [[:get "/remote-user/*"]                           #'stream/remote-user]
    [[:get "/settings/profile"]                        #'user/profile]
    [[:delete "/subscriptions/:id"]                    #'subscription/delete]
    [[:delete "/users/:id"]                            #'user/delete]
    [[:get "/users/:id"]                               #'stream/remote-profile]
    [[:post "/users/:id/discover"]                     #'user/discover]
    [[:post "/users/:id/update"]                       #'user/fetch-updates]
    [[:post "/users/:id/update-hub"]                   #'user/update-hub]
    [[:post "/users/:id/push/subscribe"]               #'push/subscribe]
    [[:get "/:username"]                               #'stream/user-timeline]
    [[:get "/:username.:format"]                       #'stream/user-timeline]

    ;; FIXME: Updating the user should probably post to a different uri
    [[:post "/:username"]                              #'user/update]
    [[:get "/:username/all"]                           #'stream/index]
    [[:get "/:username/subscribers"]                   #'subscription/subscribers]
    [[:get "/:username/subscriptions"]                 #'subscription/subscriptions]]))

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
     #'subscription/subscriptions]

    [{:method :set
      :name "subscribe"
      :ns namespace/pubsub}
     #'subscription/subscribed]

    [{:method :set
      :name "unsubscribe"
      :ns namespace/pubsub}
     #'subscription/unsubscribe]

    [{:method :get
      :name "subscribers"}
     #'subscription/subscribers]

    [{:method :set
      :name "subscribers"}
     #'subscription/subscribed]

    [{:method :result
      :name "subscription"
      :ns namespace/pubsub}
     #'subscription/remote-subscribe-confirm]

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

    [{:method :result
      :pubsub false} #'domain/ping-response]]))

(def #^:dynamic *standard-middleware*
  [#'wrap-log-request
   #'wrap-log-params])

(def http-predicates
  [#'http-serialization?
   [#'request-method-matches?
    #'path-matches?]])

(def xmpp-predicates
  [#'xmpp-serialization?
   [#'type-matches?
    #'node-matches?
    #'name-matches?
    #'ns-matches?]])

(compojure/defroutes all-routes
  (compojure/GET "/favicon.ico" request
                 (response/file-response "favicon.ico"))
  (compojure/GET "/robots.txt" _
                 (response/file-response "public/robots.txt"))
  (wrap-log-request
   (resolve-routes [http-predicates] http-routes))
  (compojure/GET "/main/events" _
                 (http/wrap-aleph-handler stream/stream-handler))
  (route/not-found (not-found-msg)))

(def app
  (http/wrap-ring-handler
   (-> all-routes
       (file/wrap-file "resources/public/")
       file-info/wrap-file-info
       wrap-keyword-params
       wrap-nested-params
       wrap-multipart-params
       wrap-params
       wrap-flash
       wrap-user-binding
       middleware/wrap-http-serialization
       wrap-database
       wrap-session
       wrap-authentication-handler
       stacktrace/wrap-stacktrace)))
