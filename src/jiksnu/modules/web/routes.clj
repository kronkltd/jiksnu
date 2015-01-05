(ns jiksnu.modules.web.routes
  (:require [aleph.http :as http]
            [ciste.config :refer [config]]
            [ciste.initializer :refer [definitializer]]
            [ciste.middleware :as middleware]
            [ciste.routes :refer [resolve-routes]]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.tools.reader.edn :as edn]
            [clojurewerkz.route-one.core :refer [*base-url*]]
            [compojure.core :as compojure :refer [GET]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.core :as h]
            [jiksnu.actions.stream-actions :as stream]
            [jiksnu.model :as model]
            [jiksnu.modules.web.middleware :as jm]
            [jiksnu.predicates :as predicates]
            [jiksnu.registry :as registry]
            [jiksnu.modules.web.routes.admin-routes :as routes.admin]
            [jiksnu.modules.web.sections.layout-sections :as sections.layout]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [ring.middleware.file :as file]
            [ring.middleware.file-info :as file-info]
            [ring.middleware.flash :refer [wrap-flash]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.stacktrace :as stacktrace]
            [monger.ring.session-store :as ms]
            [slingshot.slingshot :refer [throw+]])
  (:import java.io.PushbackReader
           javax.security.auth.login.LoginException))

(defn not-found-msg
  []
  "Not Found")

(defn load-pages!
  [route-sym]
  (when-let [page-fn (try
                       (ns-resolve route-sym 'pages)
                       (catch Exception ex
                         (log/error ex)))]
    (when-let [matchers (page-fn)]
      (dosync
       (alter predicates/*page-matchers* concat matchers)))))

(defn load-sub-pages!
  [route-sym]
  (when-let [page-fn (try
                       (ns-resolve route-sym 'sub-pages)
                       (catch Exception ex
                         (log/error ex))) ]
    (when-let [matchers (page-fn)]
      (dosync
       (alter predicates/*sub-page-matchers* concat matchers)))))

(defn load-routes!
  [route-sym]
  (when-let [route-fn (try
                        (ns-resolve route-sym 'routes)
                        (catch Exception ex
                          (log/error ex)))]
    (route-fn)))

(defn load-module
  [module-name]
  (let [route-sym (symbol (format "jiksnu.modules.web.routes.%s-routes" module-name))]
    (log/debug (str "Loading routes for: " route-sym))

    (try
      (require route-sym)
      (load-pages! route-sym)
      (load-sub-pages! route-sym)
      (load-routes! route-sym)

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

(def http-routes
  (->> registry/action-group-names
       (map load-module)
       (reduce concat)
       make-matchers))

(defn serve-template
  [request]
  (let [template-name (:* (:params request))
        path (str "templates/" (log/spy :info template-name) ".edn")
        url (io/resource path)
        reader (PushbackReader. (io/reader url))
        data (edn/read reader)]
    {:headers {"Content-Type" "text/html"}
     :body (h/html data) }))

(defn index
  [_]
  (sections.layout/page-template-content {} {}))

(compojure/defroutes all-routes
  (GET "/templates/*" [] #'serve-template)
  (compojure/GET "/websocket" _
                 (http/wrap-aleph-handler stream/websocket-handler))
  (compojure/GET "/" request
                 (when (:websocket? request)
                   ((http/wrap-aleph-handler stream/websocket-handler) request)))
  (compojure/GET "/main/events" [] stream/stream-handler)
  (compojure/ANY "/admin*" request
                 (if (session/is-admin?)
                   ((middleware/wrap-log-request
                     (resolve-routes [predicates/http] routes.admin/admin-routes))
                    request)
                   ;; TODO: move this somewhere else
                   (throw+ {:type :authentication :message "Must be admin"})))
  (middleware/wrap-log-request
   (resolve-routes [predicates/http] http-routes))
  (GET "/*" [] #'index)
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
          jm/wrap-oauth-user-binding
          jm/wrap-authorization-header
          (handler/site {:session {:store (ms/session-store)}})
          jm/wrap-stacktrace
          (wrap-resource "public")
          file-info/wrap-file-info
          ))))

  )
