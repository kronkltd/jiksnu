(ns jiksnu.modules.web.helpers
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.tools.reader.edn :as edn]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.core :as h]
            [jiksnu.predicates :as predicates]
            [jiksnu.registry :as registry]
            [jiksnu.modules.web.helpers :as helpers]
            [jiksnu.modules.web.sections.layout-sections :as sections.layout]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]])
  (:import java.io.PushbackReader))

(defn not-found-msg
  []
  "Not Found")

(defn try-resolve
  [route-sym fn-sym]
  (try
    (ns-resolve route-sym fn-sym)
    (catch Exception ex
      (log/error ex))))

(defn load-pages!
  [route-sym]
  (when-let [page-fn (try-resolve route-sym 'pages)]
    (when-let [matchers (page-fn)]
      (dosync
       (alter predicates/*page-matchers* concat matchers)))))

(defn load-sub-pages!
  [route-sym]
  (when-let [page-fn (try-resolve route-sym 'sub-pages) ]
    (when-let [matchers (page-fn)]
      (dosync
       (alter predicates/*sub-page-matchers* concat matchers)))))

(defn load-routes!
  [route-sym]
  (when-let [f (try-resolve route-sym 'routes)]
    (f)))

(defn trigger-on-loaded!
  [route-sym]
  (when-let [f (try-resolve route-sym 'on-loaded)]
    (f)))

(defn load-group
  [group]
  (let [route-sym (symbol (format "jiksnu.modules.web.routes.%s-routes" group))]
    (log/debug (str "Loading routes for: " route-sym))

    (try
      (require route-sym)
      (load-pages! route-sym)
      (load-sub-pages! route-sym)
      (trigger-on-loaded! route-sym)
      (load-routes! route-sym)
      (catch Exception ex
        (log/error ex)
        (throw+ ex)
        ))))

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

(defn serve-template
  [request]
  (let [template-name (:* (:params (log/spy :info request)))
        path (str "templates/" template-name ".edn")
        url (io/resource path)
        reader (PushbackReader. (io/reader url))
        data (edn/read reader)]
    {:headers {"Content-Type" "text/html"}
     :body (h/html data) }))

(defn index
  [_]
  (sections.layout/page-template-content {} {}))

(defn close-connection
  [handler]
  (fn [request]
    (if-let [response (handler request)]
      (assoc-in response [:headers "Connection"] "close"))))

