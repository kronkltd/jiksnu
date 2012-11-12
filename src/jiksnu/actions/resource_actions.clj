(ns jiksnu.actions.resource-actions
  (:use [ciste.core :only [defaction]]
        [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?> -?>>]]
        [jiksnu.actions :only [invoke-action]]
        [jiksnu.transforms :only [set-_id set-updated-time set-created-time]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clj-http.client :as client]
            [clj-statsd :as s]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [lamina.core :as l]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.namespace :as ns]
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
      set-_id
      set-updated-time
      set-created-time))

(defaction create
  [params]
  (let [item (prepare-create params)]
    (s/increment "resources created")
    (model.resource/create item)))

(defaction find-or-create
  [params & [{tries :tries :or {tries 1} :as options}]]
  (if-let [
           item (or (model.resource/fetch-by-url (:url params))
                    (try
                      (create params)
                      (catch RuntimeException ex)))]
    item
    (if (< tries 3)
      (do
        (log/info "recurring")
        (find-or-create params (assoc options :tries (inc tries))))
      (throw+ "Could not create conversation"))))

(defaction delete
  "Delete the resource"
  [item]
  (if-let [item (prepare-delete item)]
    (do (model.resource/delete item)
        item)
    (throw+ "Could not delete record")))

(defn response->tree
  [response]
  (enlive/html-resource (StringReader. (:body response))))

(defn get-links
  [tree]
  (enlive/select tree [:link]))

(def index*
  (model/make-indexer 'jiksnu.model.resource
                      :sort-clause {:updated -1}))

(defaction index
  [& args]
  (apply index* args))

(defn process-response
  [item response]
  (let [content-type (get-in response [:headers "content-type"])
        status (:status response)]
    (model.resource/set-field! item :status status)
    (model.resource/set-field! item :contentType content-type)))

(defn update*
  [item & [options]]
  (let [url (:url item)]
    (log/debugf "updating resource: %s" url)
    (let [response  (client/get url)]
      (process-response item response)
      response)))

(defaction update
  [item]
  (update* item)
  item)

(defaction discover
  [item]
  (log/debugf "discovering resource: %s" item)
  (let [response (update* item)]
    (model.resource/fetch-by-id (:_id item))))

(defaction show
  [item]
  item)

(l/receive-all
 model/pending-resources
 (fn [[url ch]]
   (l/enqueue ch (find-or-create {:url url}))))

(definitializer
  (model.resource/ensure-indexes)

  (require-namespaces
   ["jiksnu.filters.resource-filters"
    "jiksnu.sections.resource-sections"
    ;; "jiksnu.triggers.resource-triggers"
    "jiksnu.views.resource-views"]))
