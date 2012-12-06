(ns jiksnu.actions.resource-actions
  (:use [ciste.core :only [defaction]]
        [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?> -?>>]]
        [jiksnu.actions :only [invoke-action]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clj-http.client :as client]
            [clj-statsd :as s]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [lamina.core :as l]
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
            [net.cgrand.enlive-html :as enlive])
  (:import java.io.StringReader))

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
      transforms/set-updated-time
      transforms/set-created-time))

(declare update)

(defaction create
  [params]
  (let [params (prepare-create params)]
    (if-let [item (model.resource/create params)]
      (do
        #_(future (update item))
        item)
      (throw+ "Could not create record"))))

(defaction find-or-create
  [params & [{tries :tries :or {tries 1} :as options}]]
  (if-let [item (or (model.resource/fetch-by-url (:url params))
                    (try
                      (create params)
                      (catch RuntimeException ex
                        (.printStackTrace ex))))]
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

(defn response->tree
  [response]
  (enlive/html-resource (StringReader. (:body response))))

(defn get-links
  [tree]
  (enlive/select tree [:link]))

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
  (if-let [existing-link (and nil (model.resource/get-link item
                                                           (:rel link)
                                                           (:type link)))]
    item
    (add-link* item link)))

(defn meta->property
  "Convert a meta element to a property map"
  [meta]
  (let [attrs (:attrs meta)
        property (:property attrs)
        content (:content attrs)]
    (when (and property content)
      {property content})))

(defn get-meta-properties
  "Get a map of all the meta properties in the document"
  [tree]
  (->> (enlive/select tree [:meta])
       (map meta->property)
       (reduce merge)))

(defn process-response-html
  [item response]
  (log/info "parsing html content")
  (let [tree (response->tree response)]
    (let [properties (get-meta-properties tree)]
      (model.resource/set-field! item :properties properties))
    (let [title (first (map (comp first :content) (enlive/select tree [:title])))]
      (model.resource/set-field! item :title title))
    (let [links (get-links tree)]
      (doseq [link links]
        (add-link item (:attrs link))))))

(defn process-response
  [item response]
  (let [content-str (get-in response [:headers "content-type"])
        status (:status response)
        location (get-in response [:headers "location"])]
    (model.resource/set-field! item :status status)
    (when location
      (let [resource (ops/get-resource location)]
        (model.resource/set-field! item :location location)))
    (let [[content-type rest] (string/split content-str #"; ?")]
      (if (seq rest)
        (let [encoding (string/replace rest "charset=" "")]
          (when (seq encoding)
            (model.resource/set-field! item :encoding encoding))))
      (model.resource/set-field! item :contentType content-type)

      (condp = content-type
        "text/html" (process-response-html item response)
        (log/infof "unknown content type: %s" content-type)))))

(def user-agent "Jiksnu Resource Fetcher (http://github.com/duck1123/jiksnu)")

(defn update*
  [item & [options]]
  (if-not (:local item)
    (let [url (:url item)]
      (log/debugf "updating resource: %s" url)
      (let [response (client/get url {:throw-exceptions false
                                      :headers {"User Agent" user-agent}
                                      :insecure? true})]
        (future
          (process-response item response))
        response))
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
  (deliver p (find-or-create {:url url})))

(defn handle-pending-update-resources
  [[p item]]
  (deliver p (update* item)))

(l/receive-all ch/pending-get-resource     handle-pending-get-resource)
(l/receive-all ch/pending-update-resources handle-pending-update-resources)

(definitializer
  (model.resource/ensure-indexes)

  (require-namespaces
   ["jiksnu.filters.resource-filters"
    "jiksnu.sections.resource-sections"
    ;; "jiksnu.triggers.resource-triggers"
    "jiksnu.views.resource-views"]))
