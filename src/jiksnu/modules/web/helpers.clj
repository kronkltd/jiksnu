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

(def types
  {:json "application/json"
   :html "text/html"})

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

(defn index
  [_]
  (sections.layout/page-template-content {} {}))

(defn angular-resource-handle-ok
  [{{method :request-method} :request
    resource :resource :as ctx}]
  (let [k (keyword (str "handle-ok-" (name method)))]
    (timbre/infof "Method: %s" method)
    ((get resource k index) ctx)))

(defn angular-resource-description
  [r]
  {:summary (or (when-let [state (get-in r [:methods :get :state])]
                  (str "state: " state))
                "Angular Template")
   :description "This is a double for an angular route. Requesting this page directly will return the angular page."
   :contentType "text/html"
   :responses
   {"200" {:description (or (get-in r [:methods :get :description])
                            (:description r)
                            "Angular Template")
           :schema {:type "string"}}}})

(defn load-group
  [group]
  (let [route-sym (symbol (format "jiksnu.modules.web.routes.%s-routes" group))]
    (if (try (require route-sym) true (catch FileNotFoundException _ nil))
      (do
        #_
        (timbre/with-context {:sym (str route-sym)}
          (timbre/debugf "Loading route group - %s" route-sym))
        (try
          (core.helpers/load-pages! route-sym)
          (core.helpers/load-sub-pages! route-sym)
          (catch Exception ex
            (timbre/error ex "Failed to load routes")
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

(defn page-exists?
  [{resource :resource}]
  (if-let [page-name (when-let [page-name-fn (:page resource)] (page-name-fn))]
    (do
      (timbre/with-context {:page page-name}
        (timbre/debugf "Fetching Page - %s" page-name))
      {:data (actions/get-page page-name)})
    (throw+ {:message "Resource does not define a page name"})))

(defn subpage-exists?
  [{{:keys [subpage target target-model]} :resource
    {{id :_id} :route-params} :request :as ctx}]
  #_
  (timbre/infof "fetching subpage - %s(%s)" target-model subpage)
  (when-let [item (actions/get-model (target-model) id)]
    {:data (actions/get-sub-page item (subpage))}))

(defn get-user
  "Gets the user from the context"
  [{{{username :username} :route-params} :request}]
  (model.user/get-user username))

(defn item-resource-delete!
  "Generic item delete handler for page items"
  [ctx]
  (let [{{{id :_id} :route-params} :request} ctx]
    ((get-handler ctx 'delete) (:data ctx))))

(defn item-resource-malformed?
  [ctx]
  (let [fetcher (get-handler ctx 'fetch-by-id)]
    (if-let [id (get-in ctx [:request :route-params :_id])]
      [false {:data (fetcher id)}]
      true)))

(defn item-resource-authorized?
  [ctx]
  (let [{{method :request-method} :request} ctx
        username (session/current-user-id)]
    [(if (#{:put :delete} method) (boolean (seq username)) true)
     {:username username}]))

;;; Resource Mixins

(defn ciste-resource
  "route mixin for paths that use ciste"
  [{:keys [available-formats] :as resource}]
  (-> {:available-media-types (mapv types available-formats)}
      (merge resource)
      mixin/handled-resource))

(defn page-resource
  "route mixin for paths that operate on a page"
  [{action-ns :ns :as resource}]
  (if-let [action-sym (ns-resolve action-ns 'index)]
    (-> {:allowed-methods [:get :post :delete]
         :available-formats [:json]
         :available-media-types ["application/json"]
         :exists? page-exists?
         ;; FIXME: Return actual count
         :count (constantly 4)}
        (merge resource)
        ciste-resource)
    (throw+ "Could not resolve index action")))

(defn item-resource
  "Route mixin for resources that represent a page item"
  [resource]
  (if-let [action-ns-sym (:ns resource)]
    (do
      (require action-ns-sym)
      (-> {:respond-with-entity? false
           :allowed-methods [:get :put :delete]
           :available-formats [:json :clj]
           :methods {:get {:summary "Index Item"
                           :contentType "application/json"
                           :description "view of an item"
                           :responses {"200" {:description "Valid response"
                                              :schema {:type "string"}}}}}
           :exists? :data
           :malformed? item-resource-malformed?
           :delete! item-resource-delete!
           :authorized? item-resource-authorized?}
          (merge resource)
          ciste-resource
          mixin/item-resource))
    (throw+ {:message "Ns not defined"})))

(defn subpage-resource
  "route mixin for paths that operate on a subpage"
  [resource]
  (-> {:allowed-methods [:get]
       :available-formats [:json]
       :exists? subpage-exists?}
      (merge resource)
      ciste-resource))

(defn as-collection-resource
  [{:keys [indexer fetcher collection-type] :as resource}]
  (-> {:allowed-methods [:get :post]
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
      (merge resource)
      mixin/handled-resource))

(defn angular-resource
  [{:keys [methods] :as r}]
  (let [get-method (:get methods)
        description (merge (angular-resource-description r) get-method)]
    (-> {:exists? true
         :handle-ok angular-resource-handle-ok
         :available-media-types (mapv types [:html])}
        (merge r)
        (assoc-in [:methods :get] description))))
