(ns jiksnu.modules.web.helpers
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.tools.reader.edn :as edn]
            [hiccup.core :as h]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.as.sections.user-sections :refer [format-collection]]
            [jiksnu.modules.core.actions :as actions]
            [jiksnu.modules.http.resources :refer [defresource]]
            [jiksnu.modules.web.sections.layout-sections :as sections.layout]
            [jiksnu.predicates :as predicates]
            [jiksnu.registry :as registry]
            [octohipster.mixins :as mixin]
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre])
  (:import java.io.PushbackReader
           (java.io FileNotFoundException)))

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

(defn not-found-msg
  []
  "Not Found")

(defn try-resolve
  [route-sym fn-sym]
  (try
    (ns-resolve route-sym fn-sym)
    (catch Exception ex
      (timbre/error ex))))

(defn load-pages!
  [route-sym]
  (when-let [page-fn (try-resolve route-sym 'pages)]
    (when-let [matchers (page-fn)]
      (dosync
       (alter predicates/*page-matchers* concat matchers)))))

(defn load-sub-pages!
  [route-sym]
  (if-let [page-fn (try-resolve route-sym 'sub-pages)]
    (if-let [matchers (page-fn)]
      (dosync
       (alter predicates/*sub-page-matchers* concat matchers))
      (timbre/warn "No matchers returned"))
    #_(timbre/warnf "Could not load subpage function - %s" route-sym)))

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
    (if (try (require route-sym) true (catch FileNotFoundException _ nil))
      (do
        #_(timbre/with-context {:sym (str route-sym)}
            (timbre/debugf "Loading route group - %s" route-sym))
        (try
          (load-pages! route-sym)
          (load-sub-pages! route-sym)
          (trigger-on-loaded! route-sym)
          (load-routes! route-sym)
          (catch Exception ex
            (timbre/error "Failed to load routes" ex)
            (throw+ ex))))
      (timbre/warnf "Could not require group - %s" group))))

(defn load-routes
  []
  (doseq [group registry/action-group-names]
    (load-group group)))

(defn make-matchers
  [handlers]
  (timbre/debug "making matchers")
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
  [{{template-name :*} :params}]
  (let [path (str "templates/" template-name ".edn")
        url (io/resource path)
        reader (PushbackReader. (io/reader url))
        data (edn/read reader)]
    {:headers {"Content-Type" "text/html"}
     :body (h/html data)}))

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

(defn handle-ok
  [ctx]
  (timbre/info "Handling ok")
  (condp = (get-in ctx [:representation :media-type])
    (types :html)
    (index (:request ctx))

    (types :json)
    (json/json-str (:page ctx))))

(defn ciste-resource
  "route mixin for paths that use ciste"
  [{:keys [available-formats] :as resource}]
  (let [media-types (mapv types available-formats)]
    (-> resource mixin/handled-resource
        (assoc :available-media-types media-types))))

(defn page-resource
  "route mixin for paths that operate on a page"
  [{action-ns :ns :as r}]
  (if-let [action-sym (ns-resolve action-ns 'index)]
    (merge {:allowed-methods [:get :post :delete]
            :exists? (fn [_]
                       #_(timbre/with-context {:ns (str action-ns)}
                           (timbre/debugf "Fetching Page - %s" (:name r)))
                       (when-let [action-var (var-get action-sym)]
                         [true {:data (action-var)}]))
            ;; FIXME: Return actual count
            :count (constantly 4)}
           (ciste-resource r))
    (throw+ "Could not resolve index action")))

(defn subpage-exists?
  [{:keys [subpage target target-model]}
   {{{id :_id} :route-params} :request :as ctx}]
  #_(timbre/infof "fetching subpage - %s(%s)" target-model subpage)
  (when-let [item (if target
                    (target ctx)
                    (actions/get-model target-model id))]
    {:data (actions/get-sub-page item subpage)}))

(defn subpage-resource
  "route mixin for paths that operate on a subpage"
  [resource]
  (-> resource
      (assoc :allowed-methods [:get])
      (assoc :available-formats [:json])
      ciste-resource
      (assoc :exists? #(subpage-exists? resource %))))

(defn get-user
  "Gets the user from the context"
  [{{{username :username} :route-params} :request}]
  (model.user/get-user username))

(defn as-collection-resource
  [{:keys [indexer fetcher collection-type] :as resource}]
  (-> (merge {:allowed-methods [:get :post]
              :available-media-types ["application/json"]
              :can-put-to-missing? false
              :methods {:get {:summary (str "Get Collection of " collection-type)}
                        :post {:summary (str "Add to collection of " collection-type)}}
              :collection-key :collection
              :exists? (fn [ctx]
                         (let [user (get-user ctx)
                               page (-> (indexer ctx user)
                                        (assoc :objectTypes collection-type)
                                        (update :items #(map fetcher %)))]
                           {:data (format-collection user page)}))
              :new? false
              :parameters {:username (path :model.user/username)}
              :respond-with-entity? true
              :schema {:type "object"}}
             resource)
      mixin/handled-resource))

(defn angular-resource
  [{:keys [methods] :as r}]
  (let [get-method (:get methods)]
    (-> {:exists? true
         :handle-ok index
         :available-media-types (mapv types [:html])}
        (merge r)
        (assoc-in
         [:methods :get]
         (merge
          {:summary (or (when-let [state (get-in r [:methods :get :state])]
                          (str "state: " state))
                        "Angular Template")
           :description "This is a double for an angular route. Requesting this page directly will return the angular page."
           :contentType "text/html"
           :responses
           {"200" {:description (or (get-in r [:methods :get :description])
                                    (:description r)
                                    "Angular Template")
                   :headers {"Content-Type" {:description "The Content Type"}}}}}
          get-method)))))

(defn make-page-handler
  [& {:as opts}]
  (let []
    (->> opts
         page-resource
         (mapcat (fn [[k v]] [k v])))))
