(ns jiksnu.modules.web.routes
  (:require [aleph.http :as http]
            [ciste.config :refer [config]]
            [ciste.initializer :refer [definitializer]]
            [ciste.middleware :as middleware]
            [ciste.routes :refer [resolve-routes]]
            [clojure.tools.logging :as log]
            [clojurewerkz.route-one.core :refer [*base-url*]]
            [compojure.core :as compojure :refer [GET]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [jiksnu.actions.stream-actions :as stream]
            [jiksnu.model :as model]
            [jiksnu.modules.web.middleware :as jm]
            [jiksnu.predicates :as predicates]
            [jiksnu.registry :as registry]
            [jiksnu.modules.web.actions.template-actions :as templates]
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
    (log/debug (str "Loading routes for: " route-sym))

    (try
      (require route-sym)

      (when-let [page-fn (try
                           (ns-resolve route-sym 'pages)
                           (catch Exception ex
                             (log/error ex)))]
        (when-let [matchers (page-fn)]
          (dosync
           (alter predicates/*page-matchers* concat matchers))))

      (when-let [page-fn (try
                           (ns-resolve route-sym 'sub-pages)
                           (catch Exception ex
                             (log/error ex))) ]
        (when-let [matchers (page-fn)]
          (dosync
           (alter predicates/*sub-page-matchers* concat matchers))))

      (when-let [route-fn (try
                            (ns-resolve route-sym 'routes)
                            (catch Exception ex
                              (log/error ex)))]
        (route-fn))

      (catch Exception ex
        (log/error ex)))

    ))

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

(defn template-routes
  []
  (compojure/routes
   (GET "/partials/right-column.html"        [] #'templates/right-column)
   (GET "/partials/admin-conversations.html" [] #'templates/admin-conversations)
   (GET "/partials/new-post.html"            [] #'templates/new-post)
   ))

(def http-routes
  (->> registry/action-group-names
       (map load-module)
       (reduce concat)
       make-matchers))

(compojure/defroutes all-routes
  (compojure/GET "/websocket" _
                 (http/wrap-aleph-handler stream/websocket-handler))
  (compojure/GET "/" request
                 (when (:websocket? request)
                   ((http/wrap-aleph-handler stream/websocket-handler) request)))
  (compojure/GET "/main/events" [] stream/stream-handler)
  (template-routes)
  (compojure/ANY "/admin*" request
                 (if (session/is-admin?)
                   ((middleware/wrap-log-request
                     (resolve-routes [predicates/http] routes.admin/admin-routes)) request)
                   ;; TODO: move this somewhere else
                   (throw+ {:type :authentication :message "Must be admin"})))
  (middleware/wrap-log-request
   (resolve-routes [predicates/http] http-routes))
  (GET "/*" [] #'templates/index)
  (route/not-found (not-found-msg)))

(declare app)


(defn close-connection
  [handler]
  (fn [request]
    (if-let [response (handler request)]
      (assoc-in response [:headers "Connection"] "close"))))

(definitializer
  (let [url (format "http://%s" (config :domain))]
    (alter-var-root #'*base-url*
                    (constantly url)))

  (def app
    (http/wrap-ring-handler
     (compojure/routes
      (route/resources "/webjars/" {:root "META-INF/resources/webjars/"})
      (-> all-routes
          jm/wrap-authentication-handler
          ;; (file/wrap-file "resources/public/")
          ;; file-info/wrap-file-info
          jm/wrap-user-binding
          jm/wrap-dynamic-mode
          jm/wrap-oauth-user-binding
          jm/wrap-authorization-header
          (handler/site {:session {:store (ms/session-store)}})
          jm/wrap-stacktrace
          (wrap-resource "public")
          file-info/wrap-file-info
          ))))

  (doseq [model-name registry/action-group-names]
    (doseq [module-name registry/module-names]
      (util/require-module "jiksnu.modules" module-name model-name)))

  )
