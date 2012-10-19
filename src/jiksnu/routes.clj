(ns jiksnu.routes
  (:use [ciste.routes :only [make-matchers resolve-routes]]
        [ring.middleware.flash :only [wrap-flash]]
        [slingshot.slingshot :only [throw+]])
  (:require [aleph.http :as http]
            ;; ciste.formats.default
            [ciste.middleware :as middleware]
            [ciste.predicates :as pred]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [compojure.core :as compojure]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [jiksnu.actions.stream-actions :as stream]
            [jiksnu.middleware :as jm]
            [jiksnu.routes.admin-routes :as routes.admin]
            jiksnu.sections.layout-sections
            [jiksnu.session :as session]
            [jiksnu.views :as views]
            [ring.middleware.file :as file]
            [monger.ring.session-store :as ms]
            [ring.middleware.file-info :as file-info]
            [ring.middleware.stacktrace :as stacktrace]
            [ring.util.response :as response])
  (:import javax.security.auth.login.LoginException))

(defn not-found-msg
  []
  "Not Found")

(def http-routes
  (make-matchers
   (reduce concat
           (map
     (fn [x]
       (let [route-sym (symbol (format "jiksnu.routes.%s-routes" x))]
         (require route-sym)
         (let [route-fn (ns-resolve route-sym 'routes)]
           (route-fn))))
     ["activity"
      "auth"
      "comment"
      "conversation"
      "domain"
      "favorite"
      "feed-source"
      "group"
      "like"
      "message"
      "pubsub"
      "salmon"
      "search"
      "setting"
      "site"
      "stream"
      "subscription"
      "tag"
      "user"
      ]))))

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
                    ((resolve-routes [http-predicates] routes.admin/admin-routes) request)
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
       jm/wrap-dynamic-mode
       (handler/site {:session {:store (ms/session-store)}})
       jm/wrap-stacktrace)))
