(ns jiksnu.modules.web.routes
  (:require [cemerick.friend :as friend]
            [cemerick.friend.credentials :as creds]
            [cemerick.friend.workflows :as workflows]
            [ciste.initializer :refer [definitializer]]
            [ciste.middleware :as middleware]
            [ciste.routes :refer [resolve-routes]]
            [clojure.tools.logging :as log]
            [compojure.core :refer [GET routes]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.db :refer [_db]]
            [jiksnu.model :as model]
            [jiksnu.modules.web.middleware :as jm]
            [jiksnu.registry :as registry]
            jiksnu.modules.core.formats
            jiksnu.modules.core.views
            [jiksnu.modules.http.routes :as r]
            [jiksnu.modules.web.helpers :as helpers]
            [jiksnu.modules.web.routes.admin-routes :as routes.admin]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [octohipster.documenters.schema
             :refer [schema-doc schema-root-doc]]
            [octohipster.documenters.swagger
             :refer [swagger-doc swagger-root-doc]]
            [octohipster.routes :refer [defroutes]]
            [org.httpkit.server :as server]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.flash :refer [wrap-flash]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.resource :refer [wrap-resource]]
            [monger.ring.session-store :as ms]
            [slingshot.slingshot :refer [throw+]]))

(declare app)
(declare site)

(defn load-routes
  []
  (doseq [group registry/action-group-names]
    (helpers/load-group group)))

(defn set-site
  []
  (defroutes site
    :groups
    (map (fn [[gvar group]]
           (assoc group :resources
                  (map val (get @r/resources gvar))))
         @r/groups)
    :documenters [swagger-doc swagger-root-doc
                  schema-doc schema-root-doc])
  site)

(def auth-config
  {:credential-fn actions.auth/check-credentials
   :login-uri "/main/login"
   :workflows [(workflows/http-basic :realm "/")
               (workflows/interactive-form)]})

(defn async-handler
  [request]
  (when (:websocket? request)
    (server/with-channel request channel
      (server/on-receive channel
                         (fn [body]
                           (when-let [resp (actions.stream/handle-command
                                            request channel body)]
                             (server/send! channel resp))))
      (server/on-close channel
                       (fn [status]
                         (actions.stream/handle-closed request channel status))))))

(def app
    (-> (routes
         async-handler
         (route/resources "/")
         (GET "/templates/*" [] #'helpers/serve-template)
         (-> #'site
             (friend/authenticate auth-config)
             (handler/site {:session {:store (ms/session-store @_db "session")}})))
        wrap-file-info
        wrap-content-type
        wrap-not-modified))

(definitializer
  (load-routes)
  (set-site)
  (add-watch r/resources :site (fn [k r os ns]
                                 (log/info "refreshing site")
                                 (set-site)))
  )
