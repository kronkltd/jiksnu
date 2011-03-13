(ns jiksnu.http.routes
  (:use ciste.core
        clojure.contrib.logging
        hiccup.core
        jiksnu.http.middleware
        [ring.middleware.session :only (wrap-session)])
  (:require [compojure.core :as compojure]
            [jiksnu.http.view :as view]
            (jiksnu.http.controller
             [activity-controller :as activity]
             [auth-controller :as auth]
             [inbox-controller :as inbox]
             [subscription-controller :as subscription]
             [user-controller :as user]
             [webfinger-controller :as webfinger])
            (jiksnu.http.view
             activity-view
             auth-view
             subscription-view
             user-view
             webfinger-view)
            [compojure.route :as route]))

(def #^:dynamic *standard-middleware*
  [#'wrap-log-request
   #'wrap-log-params])

(defn with-template
  [& forms]
  forms)

(dosync
 (ref-set
  *routes*
  (make-matchers
   [[:get "/"]                                       #'activity/index
    [:get "/main/register"]                          #'user/register
    [:get "/settings/profile"]                       #'user/profile
    [:get "/posts.:format"]                          #'activity/index
    [:get "/posts/new"]                              #'activity/new
    [:get "/notice/:id"]                             #'activity/show
    [:get "/notice/:id.:format"]                     #'activity/show
    [:get "/notice/:id/edit"]                        #'activity/edit
    [:get "/notice/:id/comment"]                     #'activity/new-comment
    [:post "/notice/:id/comments/update"]            #'activity/fetch-comments
    [:get "/api/statuses/user_timeline/:id.:format"] #'activity/user-timeline
    [:get "/api/statuses/public_timeline.:format"]   #'activity/index
    [:get "/admin/subscriptions"]                    #'subscription/index
    [:get "/admin/users"]                            #'user/index
    [:get "/main/login"]                             #'auth/login-page
    [:post "/main/login"]                            #'auth/login
    [:post "/main/logout"]                           #'auth/logout
    [:get  "/main/ostatus"]                          #'subscription/ostatus
    [:post "/main/subscribe"]                        #'subscription/subscribe
    [:post "/main/unsubscribe"]                      #'subscription/unsubscribe
    [:delete "/subscriptions/:id"]                   #'subscription/delete
    [:post "/notice/new"]                            #'activity/create
    [:post "/notice/:id"]                            #'activity/update
    [:delete "/notice/:id"]                          #'activity/delete
    [:post "/main/register"]                         #'user/create
    [:get "/users/:id"]                              #'user/remote-profile
    [:delete "/users/:id"]                           #'user/delete
    [:post "/:username"]                             #'user/update
    [:get "/:username/all"]                          #'inbox/index
    [:get "/:id"]                                    #'user/show
    [:get "/:id.:format"]                                    #'user/show
    [:get "/:id/subscriptions"]                      #'user/subscriptions
    [:get "/:id/subscribers"]                        #'user/subscribers
    [:get "/.well-known/host-meta"]                  #'webfinger/host-meta
    [:get "/main/xrd"]                               #'webfinger/user-meta])))

(defn method-matches?
  [request matcher]
  (and (#'compojure.core/method-matches
        (:method matcher) request)
       request))

(defn path-matches?
  [request matcher]
  (let [prepared (#'compojure.core/prepare-route (:path matcher))]
    (if-let [route-params (#'clout.core/route-matches prepared request)]
      (#'compojure.core/assoc-route-params request route-params))))

(compojure/defroutes all-routes
  (route/files "/public")
  (resolve-routes @*routes*)
  (route/not-found "/public/404.html"))

(defn app
  []
  (-> #'all-routes
      (wrap-user-debug-binding)
      (wrap-user-binding)
      (wrap-debug-binding)
      (wrap-database)
      ;; #'wrap-flash
      (wrap-session)
      (wrap-http-serialization)
      (wrap-error-catching)))
