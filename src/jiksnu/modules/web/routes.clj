(ns jiksnu.modules.web.routes
  (:require [aleph.http :as http]
            [ciste.initializer :refer [definitializer]]
            [ciste.middleware :as middleware]
            [ciste.routes :refer [resolve-routes]]
            [clojure.tools.logging :as log]
            [compojure.core :as compojure]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [jiksnu.actions.stream-actions :as stream]
            [jiksnu.model :as model]
            [jiksnu.middleware :as jm]
            [jiksnu.predicates :as predicates]
            [jiksnu.registry :as registry]
            [jiksnu.modules.web.routes.admin-routes :as routes.admin]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [ring.middleware.file :as file]
            [ring.middleware.file-info :as file-info]
            [ring.middleware.flash :refer [wrap-flash]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.stacktrace :as stacktrace]
            [monger.ring.session-store :as ms]
            [ring.util.response :as response]
            [slingshot.slingshot :refer [throw+]])
  (:import javax.security.auth.login.LoginException))

(defn not-found-msg
  []
  "Not Found")

(defn load-module
  [module-name]
  (let [route-sym (symbol (format "jiksnu.modules.web.routes.%s-routes" module-name))]
    (require route-sym)

    (when-let [page-fn (ns-resolve route-sym 'pages)]
      (when-let [matchers (page-fn)]
        (dosync
         (alter predicates/*page-matchers* concat matchers))))

    (when-let [page-fn (ns-resolve route-sym 'sub-pages)]
      (when-let [matchers (page-fn)]
        (dosync
         (alter predicates/*sub-page-matchers* concat matchers))))

    (when-let [route-fn (ns-resolve route-sym 'routes)]
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
  (->> registry/action-group-names
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


(defn close-connection
  [handler]
  (fn [request]
    (if-let [response (handler (log/spy :info request))]
      (log/spy :info (assoc-in response [:headers "Connection"] "close"))
      )
    )
  )

(definitializer
  (def app
    (http/wrap-ring-handler
     (compojure/routes
      (->  (route/resources "/webjars/" {:root "META-INF/resources/webjars/"})
           close-connection
           )

      #_(-> all-routes
          jm/wrap-authentication-handler
          jm/wrap-user-binding
          jm/wrap-dynamic-mode
          jm/wrap-oauth-user-binding
          jm/wrap-authorization-header
          (handler/site {:session {:store (ms/session-store)}})
          jm/wrap-stacktrace
          (wrap-resource "public")
          ;; (wrap-resource "/META-INF/resources")
          file-info/wrap-file-info
          jm/wrap-stat-logging))))

  (doseq [model-name registry/action-group-names]
    (doseq [module-name registry/module-names]
      (util/require-module "jiksnu.modules" module-name model-name)))

  )
