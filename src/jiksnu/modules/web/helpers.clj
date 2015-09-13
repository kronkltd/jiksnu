(ns jiksnu.modules.web.helpers
  (:require [cemerick.friend :as friend]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.tools.reader.edn :as edn]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.core :as h]
            [jiksnu.actions :as actions]
            [jiksnu.predicates :as predicates]
            [jiksnu.registry :as registry]
            [jiksnu.modules.http.resources :refer [defresource]]
            [jiksnu.modules.web.sections.layout-sections :as sections.layout]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [liberator.core :as lib]
            [octohipster.mixins :as mixin]
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
        (throw+ ex)))))

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
  (let [template-name (:* (:params request))
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

(def types
  {:json "application/json"
   :html "text/html"})

(defn exists?
  [action ctx]
  (if (= (get-in ctx [:representation :media-type])
         (types :html))
    true
    ))

(defn handle-ok
  [ctx]
  (log/info "Handling ok")
  (condp = (get-in ctx [:representation :media-type])
    (types :html)
    (index (:request ctx))

    (types :json)
    (json/json-str (:page ctx))))

(defn ciste-resource
  "route mixin for paths that use ciste"
  [{:keys [available-formats]
    :as resource}]
  (-> resource
      mixin/item-resource
      (assoc :available-media-types (mapv types available-formats))))

(defn page-resource
  "route mixin for paths that operate on a page"
  [r]
  (let [action-ns (:ns r)]
    (if-let [action (ns-resolve action-ns 'index)]
      (let [r (merge {:method-allowed? (lib/request-method-in :get :post :delete)

                      :exists? (fn exists? [ctx]
                                 (if-let [f (var-get action)]
                                   [true {:data (f)}]))
                      ;; :handle-ok handle-ok
                      :count (fn [_] 4)}
                     r)]
        (-> r
            ciste-resource))
      (throw+ "Could not resolve index action"))))

(defn subpage-resource
  "route mixin for paths that operate on a subpage"
  [{:keys [available-formats
           subpage
           target target-model]
    :as resource}]
  (-> resource
      ciste-resource
      (assoc :method-allowed? (lib/request-method-in :get :post :delete))
      (assoc :exists?
             (fn [ctx]
               (when-let [item (if target
                                 (target ctx)
                                 (actions/get-model
                                  target-model
                                  (:_id (:route-params (:request ctx)))))]
                 {:data (log/spy :info (actions/get-sub-page item subpage))})))))

(defn angular-resource
  [r]
  (let [methods (:methods r)
        get-method (:get methods)]
    (-> {:exists? true
         :handle-ok index
         :available-media-types (mapv types [:html])}
        (merge r)
        (assoc-in
         [:methods :get]
         (merge
          {:summary "Angular Template"
           :description "This is a double for an angular route. Requesting this page directly will return the angular page."
           :contentType "text/html"
           :responses {"200" {:description (or (get-in r [:methods :get :description])
                                               (:description r)
                                               "Angular Template")
                              :headers {"Content-Type" {:description "The Content Type"}}}}}
          get-method
          )))))

(defn make-page-handler
  [& {:as opts}]
  (let []
    (->> opts
         page-resource
         (mapcat (fn [[k v]] [k v])))))

(defonce parameters (ref {}))

(defn defparameter
  [k & {:as options}]
  (dosync
   (alter parameters assoc k options)))

(defn get-parameter
  [k]
  (k @parameters))

(defn path
  ([k] (path k nil))
  ([k required?]
   (merge (get-parameter k)
          {:in "path"})))

