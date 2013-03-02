(ns jiksnu.actions.resource-actions
  (:use [ciste.core :only [defaction]]
        [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?> -?>>]]
        [jiksnu.actions :only [invoke-action]]
        [lamina.executor :only [task]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clj-http.client :as client]
            [clj-statsd :as s]
            [clj-time.core :as time]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [lamina.core :as l]
            [lamina.trace :as trace]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.channels :as ch]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.namespace :as ns]
            [jiksnu.ops :as ops]
            [jiksnu.templates :as templates]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.resource-transforms :as transforms.resource]
            [monger.collection :as mc]
            [net.cgrand.enlive-html :as enlive]))

(def user-agent "Jiksnu Resource Fetcher (http://github.com/duck1123/jiksnu)")

(defonce delete-hooks (ref []))

(defn prepare-delete
  ([item]
     (prepare-delete item @delete-hooks))
  ([item hooks]
     (if (seq hooks)
       (recur ((first hooks) item) (rest hooks))
       item)))

(defn prepare-create
  [params]
  (-> params
      transforms/set-_id
      transforms.resource/set-local
      transforms.resource/set-domain
      transforms.resource/set-location
      transforms/set-updated-time
      transforms/set-created-time))

(defaction create
  [params]
  (let [params (prepare-create params)]
    (if-let [item (model.resource/create params)]
      item
      (throw+ "Could not create record"))))

(defaction find-or-create
  [params & [{tries :tries :or {tries 1} :as options}]]
  (if-let [item (or (model.resource/fetch-by-url (:url params))
                    (try
                      (create params)
                      (catch RuntimeException ex
                        (trace/trace "errors:handled" ex))))]
    item
    (if (< tries 3)
      (do
        (log/info "recurring")
        (find-or-create params (assoc options :tries (inc tries))))
      (throw+ "Could not create resource"))))

(defaction delete
  "Delete the resource"
  [item]
  (if-let [item (prepare-delete item)]
    (do (model.resource/delete item)
        item)
    (throw+ "Could not delete resource")))

(def index*
  (templates/make-indexer 'jiksnu.model.resource
                      :sort-clause {:updated -1}))

(defaction index
  [& args]
  (apply index* args))

(defaction add-link*
  [item link]
  (mc/update "resources" {:_id (:_id item)}
    {:$addToSet {:links link}})
  item)

(defn add-link
  [item link]
  (if-let [existing-link (model.resource/get-link item
                                                  (:rel link)
                                                  (:type link))]
    item
    (add-link* item link)))

(declare update)

(defmulti process-response-content (fn [content-type item response] content-type))

(defmethod process-response-content :default
  [content-type item response]
  (log/infof "unknown content type: %s" content-type))

(defmethod process-response-content "text/html"
  [content-type item response]
  (log/info "parsing html content")
  (let [tree (model.resource/response->tree response)]
    (let [properties (model.resource/get-meta-properties tree)]
      (model.resource/set-field! item :properties properties))
    (let [title (first (map (comp first :content) (enlive/select tree [:title])))]
      (model.resource/set-field! item :title title))
    (let [links (model.resource/get-links tree)]
      (doseq [link links]
        (add-link item (:attrs link))))))

(defn process-response
  [item response]
  (let [content-str (get-in response [:headers "content-type"])
        status (:status response)]
    (model.resource/set-field! item :status status)
    (when-let [location (get-in response [:headers "location"])]
      (let [resource (ops/get-resource location)]
        (update resource)
        (model.resource/set-field! item :location location)))
    (let [[content-type rest] (string/split content-str #"; ?")]
      (if (seq rest)
        (let [encoding (string/replace rest "charset=" "")]
          (when (seq encoding)
            (model.resource/set-field! item :encoding encoding))))
      (model.resource/set-field! item :contentType content-type)
      (process-response-content content-type item response))))

(defn update*
  [item & [options]]
  (if-not (:local item)
    (let [last-updated (:lastUpdated item)]
      (if (or (nil? last-updated)
              (time/after? (-> 5 time/minutes time/ago) last-updated))
        (let [url (:url item)]
          (log/debugf "updating resource: %s" url)
          (model.resource/set-field! item :lastUpdated (time/now))
          (let [response (client/get url {:throw-exceptions false
                                          :headers {"User-Agent" user-agent}
                                          :insecure? true})]
            (task
              (process-response item response))
            response))
        (log/warn "Resource has already been updated")))
    (log/debug "local resource does not need update")))

(defaction update
  [item]
  (update* item)
  item)

(defaction discover
  [item]
  (log/debugf "discovering resource: %s" (prn-str item))
  (let [response (update* item)]
    (model.resource/fetch-by-id (:_id item))))

(defaction show
  [item]
  item)

(defn handle-pending-get-resource
  [[p url]]
  (l/enqueue p (find-or-create {:url url})))

(defn handle-pending-update-resources
  [[p item]]
  (l/enqueue p (update* item)))

(l/receive-all ch/pending-get-resource     handle-pending-get-resource)
(l/receive-all ch/pending-update-resources handle-pending-update-resources)

(definitializer
  (model.resource/ensure-indexes)

  (require-namespaces
   ["jiksnu.filters.resource-filters"
    "jiksnu.sections.resource-sections"
    "jiksnu.triggers.resource-triggers"
    "jiksnu.views.resource-views"]))
