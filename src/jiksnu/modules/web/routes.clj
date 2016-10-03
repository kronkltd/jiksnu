(ns jiksnu.modules.web.routes
  (:require [cemerick.friend :as friend]
            [cemerick.friend.workflows :as workflows]
            [compojure.core :refer [GET routes]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.db :refer [_db]]
            [jiksnu.modules.http.actions :as http.actions]
            [jiksnu.modules.web.core :as core]
            [jiksnu.modules.web.helpers :as helpers]
            [jiksnu.modules.web.middleware :as middleware]
            [liberator.dev :refer [wrap-trace]]
            [org.httpkit.server :as server]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
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
           (handler/site {:session {:store (ms/session-store @_db "session")}})))
      wrap-file-info
      wrap-content-type
      wrap-not-modified))
