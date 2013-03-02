(ns jiksnu.routes
  (:use [ciste.config :only [config]]
        [ciste.routes :only [make-matchers resolve-routes]]
        [clj-airbrake.ring :only [wrap-airbrake]]
        [ring.middleware.flash :only [wrap-flash]]
        [slingshot.slingshot :only [throw+]])
  (:require [aleph.http :as http]
            [ciste.middleware :as middleware]
            [clj-airbrake.core :as airbrake]
            [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [compojure.core :as compojure]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [jiksnu.actions.stream-actions :as stream]
            [jiksnu.middleware :as jm]
            [jiksnu.predicates :as predicates]
            [jiksnu.routes.admin-routes :as routes.admin]
            [jiksnu.session :as session]
            [ring.middleware.file :as file]
            [monger.ring.session-store :as ms]
            [noir.util.middleware :as nm]
            [ring.middleware.file-info :as file-info]
            [ring.middleware.stacktrace :as stacktrace]
            [ring.util.response :as response])
  (:import javax.security.auth.login.LoginException))

(airbrake/set-host! "localhost:3000")

(defn not-found-msg
  []
  "Not Found")

(def route-modules
  ["activity"
   "auth"
   "comment"
   "conversation"
   "domain"
   "favorite"
   "feed-source"
   "feed-subscription"
   "group"
   "like"
   "message"
   "pubsub"
   "resource"
   "salmon"
   "search"
   "setting"
   "site"
   "stream"
   "subscription"
   "tag"
   "user"])

(defn load-module
  [module-name]
  (let [route-sym (symbol (format "jiksnu.routes.%s-routes" module-name))]
    (require route-sym)
    (let [route-fn (ns-resolve route-sym 'routes)]
      (route-fn))))

(def http-routes
  (->> route-modules
       (map load-module)
       (reduce concat)
       make-matchers))

(compojure/defroutes all-routes
  (compojure/GET "/api/help/test.json" _ "OK")
  (compojure/ANY "/admin*" request
                 (if (session/is-admin?)
                   ((middleware/wrap-log-request
                     (resolve-routes [predicates/http] routes.admin/admin-routes)) request)
                   ;; TODO: move this somewhere else
                   (throw+ {:type :authentication :message "Must be admin"})))
  (middleware/wrap-log-request
   (resolve-routes [predicates/http] http-routes))
  (compojure/GET "/websocket" _
                 (http/wrap-aleph-handler stream/websocket-handler))
  (compojure/GET "/main/events" _
                 stream/stream-handler)
  (route/resources "/webjars" {:root "META-INF/resources/webjars"})
  (route/not-found (not-found-msg)))

(def app
  (http/wrap-ring-handler
   (-> all-routes
       jm/wrap-authentication-handler
       (file/wrap-file "resources/public/")
       file-info/wrap-file-info
       jm/wrap-user-binding
       middleware/wrap-http-serialization
       middleware/wrap-log-request
       jm/wrap-dynamic-mode
       (handler/site {:session {:store (ms/session-store)}})
       (wrap-airbrake (config :airbrake :key))
       ;; (nm/wrap-canonical-host (config :domain))
       jm/wrap-stacktrace
       jm/wrap-stat-logging)))
