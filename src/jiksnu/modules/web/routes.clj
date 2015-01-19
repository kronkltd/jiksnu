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
            ;; [octohipster.core :refer []]
            [octohipster.documenters.schema
             :refer [schema-doc schema-root-doc]]
            [octohipster.documenters.swagger
             :refer [swagger-doc swagger-root-doc]]
            [octohipster.routes :refer [defroutes]]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.flash :refer [wrap-flash]]
            ;; [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.resource :refer [wrap-resource]]
            ;; [ring.middleware.webjars :refer [wrap-webjars]]
            [monger.ring.session-store :as ms]
            [slingshot.slingshot :refer [throw+]]))

(declare app)
(declare site)

(defn load-routes
  []
  (doseq [group registry/action-group-names]
    (helpers/load-group group)))

#_(def http-routes
  (->> registry/action-group-names
       (map helpers/load-group)
       (reduce concat)
       helpers/make-matchers))

(compojure/defroutes all-routes
  (GET "/templates/*" [] #'helpers/serve-template)
  (compojure/GET "/websocket" _
                 (http/wrap-aleph-handler stream/websocket-handler))
  (compojure/GET "/" request
                 (when (:websocket? request)
                   ((http/wrap-aleph-handler stream/websocket-handler) request)))
  (compojure/GET "/main/events" [] stream/stream-handler)
  (compojure/ANY "/admin*" request
                 (if (session/is-admin?)
                   ((middleware/wrap-log-request
                     (resolve-routes [predicates/http]
                                     routes.admin/admin-routes))
                    request)
                   ;; TODO: move this somewhere else
                   (throw+ {:type :authentication :message "Must be admin"})))
  #_(middleware/wrap-log-request
   (resolve-routes [predicates/http] http-routes)))

(defn set-site
  []
  (defroutes site
    :groups
    (map (fn [[gvar group]]
           (assoc group :resources
                  (map val (get @r/resources gvar))))
         @r/groups)
    :documenters [swagger-doc swagger-root-doc
                  schema-doc schema-root-doc]))

(definitializer
  (load-routes)
  (set-site)
  (add-watch r/resources :site (fn [k r os ns]
                                 (log/info "refreshing site")
                                 (set-site)))

  (def app
    (http/wrap-ring-handler
     ;; (wrap-webjars
     (compojure/routes
      (route/resources "/webjars/" {:root "META-INF/resources/webjars/"})
      (-> all-routes
          jm/wrap-authentication-handler
           ;; (file/wrap-file "resources/public/")
           ;; wrap-file-info
           jm/wrap-user-binding
           jm/wrap-oauth-user-binding
           jm/wrap-authorization-header
           (handler/site {:session {:store (ms/session-store)}})
           jm/wrap-stacktrace
           (wrap-resource "public")
           wrap-file-info
           ;; (wrap-resource "META-INF/resources/webjars/")
           ;; wrap-content-type
           ;; wrap-not-modified
           )
      #'site
      ;; (GET "/*" [] #'helpers/index)
      (route/not-found (helpers/not-found-msg)))
     ;; )
     ))

  )


