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
            ;; [jiksnu.namespace :as ns]
            [jiksnu.routes.activity-routes :as routes.activity]
            [jiksnu.routes.admin-routes :as routes.admin]
            [jiksnu.routes.auth-routes :as routes.auth]
            [jiksnu.routes.comment-routes :as routes.comment]
            [jiksnu.routes.conversation-routes :as routes.conversation]
            [jiksnu.routes.domain-routes :as routes.domain]
            [jiksnu.routes.favorite-routes :as routes.favorite]
            [jiksnu.routes.feed-source-routes :as routes.feed-source]
            [jiksnu.routes.group-routes :as routes.group]
            [jiksnu.routes.like-routes :as routes.like]
            [jiksnu.routes.message-routes :as routes.message]
            [jiksnu.routes.pubsub-routes :as routes.pubsub]
            [jiksnu.routes.salmon-routes :as routes.salmon]
            [jiksnu.routes.search-routes :as routes.search]
            [jiksnu.routes.setting-routes :as routes.setting]
            [jiksnu.routes.site-routes :as routes.site]
            [jiksnu.routes.stream-routes :as routes.stream]
            [jiksnu.routes.subscription-routes :as routes.subscription]
            [jiksnu.routes.tag-routes :as routes.tag]
            [jiksnu.routes.user-routes :as routes.user]
            [jiksnu.routes.xmpp-routes :as routes.xmpp]
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
   (concat
    (#'routes.activity/routes)
    ;; (routes.admin/routes)
    (#'routes.auth/routes)
    (#'routes.comment/routes)
    (#'routes.conversation/routes)
    (#'routes.domain/routes)
    (#'routes.favorite/routes)
    (#'routes.feed-source/routes)
    (#'routes.group/routes)
    (#'routes.like/routes)
    (#'routes.message/routes)
    (#'routes.pubsub/routes)
    (#'routes.salmon/routes)
    (#'routes.search/routes)
    (#'routes.setting/routes)
    (#'routes.site/routes)
    (#'routes.stream/routes)
    (#'routes.subscription/routes)
    (#'routes.tag/routes)
    (#'routes.user/routes)
    ;; (#'routes.xmpp/routes)
    )))

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
