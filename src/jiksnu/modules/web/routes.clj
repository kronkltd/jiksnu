(ns jiksnu.modules.web.routes
  (:require [cemerick.friend :as friend]
            [cemerick.friend.workflows :as workflows]
            [taoensso.timbre :as timbre]
            [compojure.core :refer [GET routes]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.db :refer [_db]]
            [jiksnu.modules.http.actions :as http.actions]
            [jiksnu.modules.web.core :as core]
            [jiksnu.modules.web.helpers :as helpers]
            [liberator.dev :refer [wrap-trace]]
            [org.httpkit.server :as server]
            [ring.logger.timbre :as logger.timbre]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [monger.ring.session-store :as ms]))

(def auth-config
  {:credential-fn actions.auth/check-credentials
   :login-uri "/main/login"
   :workflows [(workflows/http-basic :realm "/")
               (workflows/interactive-form)]})

(defn async-handler
  [request]
  (when (:websocket? request)
    (server/with-channel request channel
      (http.actions/connect request channel))))

(def app
  (-> (routes
       (route/resources "/")
       (GET "/templates/*" [] #'helpers/serve-template)
       (-> (routes
            async-handler
            #'core/jiksnu-routes)
           logger.timbre/wrap-with-logger
           (wrap-trace :ui)
           (friend/authenticate auth-config)
           (handler/site {:session {:store (ms/session-store @_db "session")}})))
      wrap-file-info
      wrap-content-type
      wrap-not-modified))
