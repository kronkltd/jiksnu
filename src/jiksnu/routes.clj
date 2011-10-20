(ns jiksnu.routes
  (:use (ciste [debug :only (spy)]
               predicates routes)
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
                    [session :as session]
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
                            [subscription-actions :as sub]
                            [user-actions :as user]
                            [webfinger-actions :as webfinger])
            (jiksnu.filters activity-filters
                            auth-filters
                            comment-filters
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
                          comment-views
                          domain-views
                          push-subscription-views
                          stream-views
                          subscription-views
                          user-views
                          webfinger-views)
            (ring.middleware [file :as file]
                             [file-info :as file-info]
                             [stacktrace :as stacktrace])
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
    [[:get "/admin/subscriptions"]                     #'sub/index]
    [[:get "/admin/push/subscriptions"]                #'push/index]
    [[:post "/admin/users"]                            #'user/create]
    [[:get "/admin/users"]                             #'user/index]
    [[:get "/admin/settings"]                          #'settings/edit]]))

(def http-routes
  (make-matchers
   [[[:get  "/"]                                       #'stream/index]
    [[:get "/.well-known/host-meta"]                   #'webfinger/host-meta]
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
    [[:get "/main/ostatus"]                            #'sub/ostatus]
    [[:get "/main/ostatussub"]                         #'sub/ostatussub]
    [[:post "/main/ostatussub"]                        #'sub/ostatussub-submit]
    [[:get "/main/password"]                           #'auth/password-page]
    [[:get "/main/push/hub"]                           #'push/hub]
    [[:post "/main/push/hub"]                          #'push/hub-publish]
    [[:get "/main/push/callback"]                      #'push/callback]
    [[:post "/main/push/callback"]                     #'push/callback-publish]
    [[:get "/main/register"]                           #'user/register-page]
    [[:post "/main/register"]                          #'user/register]
    [[:post "/main/salmon/user/:id"]                   #'salmon/process]
    [[:post "/main/subscribe"]                         #'sub/subscribe]
    [[:post "/main/unsubscribe"]                       #'sub/unsubscribe]
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
    [[:post "/settings/profile"]                       #'user/update-profile]
    [[:delete "/subscriptions/:id"]                    #'sub/delete]
    [[:delete "/users/:id"]                            #'user/delete]
    [[:get "/users/:id"]                               #'stream/remote-profile]
    [[:post "/users/:id/discover"]                     #'user/discover]
    [[:get "/users/:id/subscribers"]                   #'sub/subscribers]
    [[:get "/users/:id/subscriptions"]                 #'sub/subscriptions]
    [[:post "/users/:id/update"]                       #'user/fetch-updates]
    [[:post "/users/:id/update-hub"]                   #'user/update-hub]
    [[:post "/users/:id/push/subscribe"]               #'push/subscribe]
    [[:get "/:username"]                               #'stream/user-timeline]
    [[:get "/:username.:format"]                       #'stream/user-timeline]

    ;; FIXME: Updating the user should probably post to a different uri
    [[:post "/:username"]                              #'user/update]
    [[:get "/:username/all"]                           #'stream/index]
    [[:get "/:username/subscribers"]                   #'sub/subscribers]
    [[:get "/:username/subscriptions"]                 #'sub/subscriptions]]))

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
     #'sub/subscriptions]

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
     #'sub/subscribers]

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

    [{:method :result
      :pubsub false} #'domain/ping-response]]))

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
  (wrap-authentication-handler
   (compojure/ANY "/admin*" request
                  (if (session/is-admin?)
                    ((resolve-routes [http-predicates] admin-routes) request)
                    (throw (LoginException. "Must be admin")))))
  (wrap-log-request
   (resolve-routes [http-predicates] http-routes))
  (compojure/GET "/main/events" _
                 (http/wrap-aleph-handler stream/stream-handler))
  (route/not-found (not-found-msg)))

(def app
  (http/wrap-ring-handler
   (-> all-routes
       wrap-authentication-handler
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
       stacktrace/wrap-stacktrace)))
