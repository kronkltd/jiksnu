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
             [subscription-controller :as subscription]
             [user-controller :as user])
            (jiksnu.http.view
             activity-view
             auth-view
             subscription-view
             user-view)
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
   [[:get "/"]                                        #'activity/index
    [:get "/register"]                                #'user/register
    [:get "/settings/profile"]                        #'user/profile
    [:get "/posts.:format"]                           #'activity/index
    [:get "/posts/new"]                               #'activity/new
    [:get "/posts/:id"]                               #'activity/show
    [:get "/posts/:id.:format"]                       #'activity/show
    [:get "/posts/:id/edit"]                          #'activity/edit
    [:get "/api/statuses/user_timeline/:id.:format"]  #'activity/user-timeline
    [:get "/api/statuses/public_timeline.:format"]    #'activity/index
    [:get "/admin/subscriptions"]                     #'subscription/index
    [:get "/admin/users"]                             #'user/index
    [:post "/login"]                                  #'auth/login
    [:post "/logout"]                                 #'auth/logout
    [:post "/main/subscribe"]                         #'subscription/subscribe
    [:post "/main/unsubscribe"]                       #'subscription/unsubscribe
    [:delete "/subscriptions/:id"]                    #'subscription/delete
    [:post "/posts"]                                  #'activity/create
    [:post "/posts/:id"]                              #'activity/update
    [:delete "/posts/:id"]                            #'activity/delete
    [:post "/users"]                                  #'user/create
    [:delete "/:id"]                                  #'user/delete
    [:post "/:id"]                                    #'user/update
    [:get "/:id/all"]                                 #'activity/friends-timeline
    [:get "/:id"]                                     #'user/show
    [:get "/:id/edit"]                                #'user/edit])))

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
