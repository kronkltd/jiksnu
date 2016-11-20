(ns jiksnu.modules.web.routes
  (:require [cemerick.friend :as friend]
            [cemerick.friend.workflows :as workflows]
            [compojure.core :refer [GET routes]]
            [compojure.route :as route]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.db :as db]
            [jiksnu.modules.http.actions :as http.actions]
            [jiksnu.modules.web.core :as core]
            [jiksnu.modules.web.helpers :as helpers]
            [jiksnu.modules.web.middleware :as middleware]
            [liberator.dev :refer [wrap-trace]]
            [org.httpkit.server :as server]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [monger.ring.session-store :as ms]))

(def auth-config
  {:credential-fn actions.auth/check-credentials
   :login-uri "/main/login"
   :redirect-on-auth? false
   :workflows [(workflows/http-basic :realm "/")]})

(defn async-handler
  [request]
  (when (:websocket? request)
    (server/with-channel request channel
      (http.actions/connect request channel))))

(def site-options
  (-> site-defaults
      (assoc-in [:security :anti-forgery] false)
      (assoc-in [:session :store] (ms/session-store (db/get-connection) "session"))))

(def app
  (-> (routes
       (route/resources "/")
       (route/files "/assets" {:root "/data"})
       (route/files "/vendor" {:root "node_modules"})
       (GET "/templates/*" [] #'helpers/serve-template)
       (-> (routes async-handler #'core/jiksnu-routes)
           middleware/wrap-response-logging
           (wrap-trace :ui)
           middleware/wrap-user-binding
           (friend/authenticate auth-config)
           middleware/wrap-authorization-header
           middleware/wrap-authentication-handler
           (wrap-defaults site-options)))
      middleware/wrap-stacktrace))
