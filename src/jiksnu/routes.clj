(ns jiksnu.routes
  (:use [ciste.commands :only [add-command!]]
        [ciste.config :only [config]]
        [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]
        [ciste.routes :only [resolve-routes]]
        [clj-airbrake.ring :only [wrap-airbrake]]
        [ring.middleware.flash :only [wrap-flash]]
        [ring.middleware.resource :only [wrap-resource]]
        [slingshot.slingshot :only [throw+]]
        tidy-up.core)
  (:require [aleph.http :as http]
            [ciste.middleware :as middleware]
            [clj-airbrake.core :as airbrake]
            [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [compojure.core :as compojure]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [jiksnu.actions.stream-actions :as stream]
            [jiksnu.model :as model]
            [jiksnu.middleware :as jm]
            [jiksnu.predicates :as predicates]
            [jiksnu.routes.admin-routes :as routes.admin]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [ring.middleware.file :as file]
            [monger.ring.session-store :as ms]
            [noir.util.middleware :as nm]
            [ring.middleware.file-info :as file-info]
            [ring.middleware.stacktrace :as stacktrace]
            [ring.util.response :as response])
  (:import javax.security.auth.login.LoginException))

(defn not-found-msg
  []
  "Not Found")

(defn load-module
  [module-name]
  (let [route-sym (symbol (format "jiksnu.routes.%s-routes" module-name))]
    (require route-sym)

    (when-let [page-fn (ns-resolve route-sym 'pages)]
      (when-let [matchers (page-fn)]
        (dosync
         (alter predicates/*page-matchers* concat matchers))))

    (when-let [page-fn (ns-resolve route-sym 'sub-pages)]
      (when-let [matchers (page-fn)]
        (dosync
         (alter predicates/*sub-page-matchers* concat matchers))))

    (let [route-fn (ns-resolve route-sym 'routes)]
      (route-fn))))

(defn make-matchers
  [handlers]
  (log/debug "making matchers")
  (map
   (fn [[matcher action]]
     (let [o (merge
              {:serialization :http
               :format :html}
              (if (var? action)
                {:action action}
                action))]
       (let [[method route] matcher]
         [{:method method
           ;; :format :html
           :serialization :http
           :path route} o])))
   handlers))

(def http-routes
  (->> model/action-group-names
       (map load-module)
       (reduce concat)
       make-matchers))

(compojure/defroutes all-routes
  (compojure/GET "/websocket" _
                 (http/wrap-aleph-handler stream/websocket-handler))
  (compojure/GET "/main/events" _
                 stream/stream-handler)
  (compojure/ANY "/admin*" request
                 (if (session/is-admin?)
                   ((middleware/wrap-log-request
                     (resolve-routes [predicates/http] routes.admin/admin-routes)) request)
                   ;; TODO: move this somewhere else
                   (throw+ {:type :authentication :message "Must be admin"})))
  (middleware/wrap-log-request
   (resolve-routes [predicates/http] http-routes))
  (route/not-found (not-found-msg)))

(declare app)


(definitializer
  (airbrake/set-host! (config :airbrake :host))
  (def app
    (http/wrap-ring-handler
     (compojure/routes
      (route/resources "/webjars" {:root "META-INF/resources/webjars"})
      (compojure/GET "/api/help/test.json" _ "OK")
      (-> all-routes
          jm/wrap-authentication-handler
          (file/wrap-file "resources/public/")
          file-info/wrap-file-info
          jm/wrap-user-binding
          jm/wrap-dynamic-mode
          (handler/site {:session {:store (ms/session-store)}})
          (wrap-airbrake (config :airbrake :key))
          jm/wrap-stacktrace
          jm/wrap-stat-logging
          ;; wrap-tidy-up
          ))))

  (doseq [model-name model/action-group-names]
    (require-namespaces
     "jiksnu.formats"
     )
    (doto "jiksnu.modules"
      (util/require-module "atom" model-name)
      (util/require-module "as" model-name)
      (util/require-module "rdf" model-name)
      (util/require-module "core" model-name)
      (util/require-module "web" model-name))
    )

  )
