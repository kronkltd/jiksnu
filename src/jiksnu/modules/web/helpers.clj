(ns jiksnu.modules.web.helpers
  (:require [clojure.java.io :as io]
            [clojure.tools.reader.edn :as edn]
            [hiccup.core :as h]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.as.sections.user-sections :refer [format-collection]]
            [jiksnu.modules.core.actions :as actions]
            [jiksnu.modules.core.helpers :as core.helpers]
            [jiksnu.modules.http.resources :refer [defresource]]
            [jiksnu.modules.web.sections.layout-sections :as sections.layout]
            [jiksnu.registry :as registry]
            [jiksnu.session :as session]
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
  [k]
  (merge (get-parameter k) {:in "path"}))

(defn not-found-msg
  []
  "Not Found")

(defn load-group
  [group]
  (let [route-sym (symbol (format "jiksnu.modules.web.routes.%s-routes" group))]
    (if (try (require route-sym) true (catch FileNotFoundException _ nil))
      (do
        #_(timbre/with-context {:sym (str route-sym)}
            (timbre/debugf "Loading route group - %s" route-sym))
        (try
          (core.helpers/load-pages! route-sym)
          (core.helpers/load-sub-pages! route-sym)
          (catch Exception ex
            (timbre/error "Failed to load routes" ex)
            (throw+ ex))))
      (timbre/warnf "Could not require group - %s" group))))

(defn load-routes
  []
  (doseq [group registry/action-group-names]
    (load-group group)))

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

(def types
  {:json "application/json"
   :html "text/html"})

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

(defn get-handler
  [ctx handler-sym]
  (if-let [action-ns-sym (:ns (:resource ctx))]
    (if-let [model-ns-var (some-> (action-ns-sym) (ns-resolve 'model-ns))]
      (if-let [model-ns-ref (var-get model-ns-var)]
        (or (ns-resolve model-ns-ref handler-sym)
            (throw+ {:message "Could not determine handler sym" :handler handler-sym}))
        (throw+ {:message "Model ns not found" :var model-ns-var}))
      (throw+ {:message "Model ns not defined" :action-ns (action-ns-sym)}))
    (throw+ {:message "Could not determine action namespace"})))

(defn item-resource-delete!
  "Generic item delete handler for page items"
  [ctx]
  (let [{{{id :_id} :route-params} :request} ctx]
    ((get-handler ctx 'delete) (:data ctx))))

(defn item-resource-malformed?
  [ctx]
  (let [fetcher (get-handler ctx 'fetch-by-id)
        id (get-in ctx [:request :route-params :_id])]
    [false {:data (fetcher id)}]))

(defn item-resource-authorized?
  [ctx]
  (let [{{method :request-method} :request} ctx
        username (session/current-user-id)]
    [(if (#{:put :delete} method) (boolean (seq username)) true)
     {:username username}]))

(defn item-resource
  "Route mixin for resources that represent a page item"
  [resource]
  (if-let [action-ns-sym (:ns resource)]
    (do
      (require action-ns-sym)
      (-> {:respond-with-entity? false
           :allowed-methods [:get :put :delete]
           :available-formats [:json :clj]
           :exists? :data
           :malformed? item-resource-malformed?
           :delete! item-resource-delete!
           :authorized? item-resource-authorized?}
          (merge resource)
          ciste-resource
          mixin/item-resource))
    (throw+ {:message "Ns not defined"})))

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
