(ns jiksnu.modules.web.routes
  (:require [aleph.http :as http]
            [cemerick.friend :as friend]
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
            [jiksnu.actions.stream-actions :as stream]
            [jiksnu.model :as model]
            [jiksnu.modules.web.middleware :as jm]
            [jiksnu.registry :as registry]
            [jiksnu.modules.http.routes :as r]
            [jiksnu.modules.web.helpers :as helpers]
            [jiksnu.modules.web.routes.admin-routes :as routes.admin]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [liberator.dev :refer [wrap-trace]]
            [octohipster.documenters.schema
             :refer [schema-doc schema-root-doc]]
            [octohipster.documenters.swagger
             :refer [swagger-doc swagger-root-doc]]
            [octohipster.routes :refer [defroutes]]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.flash :refer [wrap-flash]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.webjars :refer [wrap-webjars]]
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

               (workflows/interactive-form)
               ]})

(definitializer
  (load-routes)
  (set-site)
  (add-watch r/resources :site (fn [k r os ns]
                                 (log/info "refreshing site")
                                 (set-site)))

  (def app
    (-> (routes
         (route/resources "/")
         (GET "/templates/*" [] #'helpers/serve-template)
         (-> #'site
             (wrap-trace :ui :headers)
             (friend/authenticate auth-config)
             (wrap-webjars "/webjars")
             (handler/site {:session {:store (ms/session-store)}})))
        wrap-file-info
        wrap-content-type
        wrap-not-modified)))
