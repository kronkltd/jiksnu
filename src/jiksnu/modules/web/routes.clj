(ns jiksnu.modules.web.routes
  (:require [aleph.http :as http]
            [ciste.initializer :refer [definitializer]]
            [ciste.middleware :as middleware]
            [ciste.routes :refer [resolve-routes]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [compojure.core :as compojure :refer [GET]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.core :as h]
            [jiksnu.actions.stream-actions :as stream]
            [jiksnu.model :as model]
            [jiksnu.modules.web.middleware :as jm]
            [jiksnu.predicates :as predicates]
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

(definitializer
  (load-routes)
  (set-site)
  (add-watch r/resources :site (fn [k r os ns]
                                 (log/info "refreshing site")
                                 (set-site)))

  (def app
    (compojure/routes
     (compojure/GET "/" request
                    (when (:websocket? request)
                      ((http/wrap-aleph-handler stream/websocket-handler)
                       request)))
     (route/resources "/webjars/" {:root "META-INF/resources/webjars/"})
     (route/resources "/")
     (compojure/GET "/main/events" [] stream/stream-handler)
     (GET "/templates/*" [] #'helpers/serve-template)
     (wrap-trace #'site :header :ui))))
