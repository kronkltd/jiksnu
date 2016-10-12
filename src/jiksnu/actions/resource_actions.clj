(ns jiksnu.actions.resource-actions
  (:require [ciste.config :refer [config]]
            [ciste.event :refer [defkey notify]]
            [clj-time.coerce :as coerce]
            [clj-time.core :as time]
            [clojure.string :as string]
            [jiksnu.model :as model]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.resource-transforms :as transforms.resource]
            [jiksnu.util :as util]
            [manifold.deferred :as d]
            [manifold.time :as lt]
            [org.httpkit.client :as client]
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre])
  (:import jiksnu.model.Resource
           org.joda.time.DateTime))

(def model-ns 'jiksnu.model.resource)

(defkey ::resource-realized
  "Whenever a resource is realized, this event is fired"
  :schema {:item "Resource"
           :response "Map"})

(defkey ::resource-updated
  "Whenever a resource is updated")

(def user-agent "Jiksnu Resource Fetcher (http://github.com/kronkltd/jiksnu)")

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
      ;; transforms/set-_id
      (transforms/set-local :_id)
      transforms.resource/set-domain
      transforms.resource/set-location
      transforms/set-updated-time
      transforms/set-created-time
      transforms/set-no-links))

(def add-link* (templates.actions/make-add-link* model.resource/collection-name))

(defn create
  [params]
  (let [params (prepare-create params)]
    (if-let [item (model.resource/create params)]
      item
      (throw+ "Could not create record"))))

(defn find-or-create
  [params & [{tries :tries :or {tries 1} :as options}]]
  (if-let [item (or (model.resource/fetch-by-url (:_id params))
                    (try
                      (create params)
                      (catch Exception ex)))]
    item
    (if (< tries 3)
      (do
        (timbre/info "recurring")
        (find-or-create params (assoc options :tries (inc tries))))
      (throw+ "Could not create resource"))))

(defn delete
  "Delete the resource"
  [item]
  (if-let [item (prepare-delete item)]
    (do (model.resource/delete item)
        item)
    (throw+ "Could not delete resource")))

(def index*
  (templates.actions/make-indexer model-ns
                                  :sort-clause {:updated -1}))

(defn index
  [& args]
  (apply index* args))

(defn add-link
  [item link]
  (if-let [existing-link (model/get-link item (:rel link) (:type link))]
    item
    (add-link* item link)))

(defmulti process-response-content (fn [content-type item response] content-type))

(defmethod process-response-content :default
  [content-type item response]
  (timbre/infof "unknown content type: %s" content-type))

(declare update-record)

(defn process-response
  [item response]
  (let [content-str (get-in response [:headers "content-type"])
        status (:status response)]
    (model.resource/set-field! item :status status)
    (when-let [location (get-in response [:headers "location"])]
      (let [resource (find-or-create {:_id location})]
        (update-record resource)
        (model.resource/set-field! item :location location)))
    (let [[content-type rest] (string/split content-str #"; ?")]
      (if (seq rest)
        (let [encoding (string/replace rest "charset=" "")]
          (when (seq encoding)
            (model.resource/set-field! item :encoding encoding))))
      (model.resource/set-field! item :contentType content-type)
      (process-response-content content-type item response))))

(defn needs-update?
  [item options]
  (let [last-updated (:lastUpdated item)]
    (and (not (:local item))
         (or (:force options)
             (nil? last-updated)
             (time/after? (-> 5 time/minutes time/ago)
                          (coerce/to-date-time last-updated))))))

(defn handle-unauthorized
  [item response]
  (model.resource/set-field! item :requiresAuth true)
  nil)

(defn update*
  "Fetches the resource and returns a result channel or nil.

  The channel will receive the body of fetching this resource."
  [item & [options]]
  {:pre [(instance? Resource item)]}
  (let [d (d/deferred)
        url (:_id item)
        actor (session/current-user)
        ^DateTime date (time/now)]
    (if (needs-update? item options)
      (if (:requiresAuth item)
        ;; auth required
        (throw+ "Resource requires authorization")
        ;; no auth required
        (let [res (d/deferred)]
          (d/timeout! res (lt/seconds 30))
          (let [auth-string (string/join
                             " "
                             ["Dialback"
                              (format "host=\"%s\"" (config :domain))
                              (format "token=\"%s\"" "4430086d")])
                options {:headers {"User-Agent" user-agent
                                   "date" (util/date->rfc1123 (.toDate date))
                                   "authorization" auth-string}}]
            (notify ::resource-updated {:item item})
            (timbre/infof "Fetching %s" url)
            (client/get url options
                        (fn [response]
                          (notify ::resource-realized
                                  {:item item
                                   :response response})
                          (model.resource/set-field! item :lastUpdated (time/now))
                          (model.resource/set-field! item :status (:status response))
                          (condp = (:status response)
                            200 response
                            401 (handle-unauthorized item response)
                            (timbre/warn "Unknown status type"))))
            res)))
      (timbre/warn "Resource does not need to be updated at this time."))))

(defn update-record
  [item]
  (update* item)
  item)

(defn discover
  [item]
  (timbre/debugf "discovering resource: %s" (prn-str item))
  (let [response (update* item)]
    (model.resource/fetch-by-id (:_id item))))

(defn show
  [item]
  item)

(defn fetch
  "Gets a possibly cached version of the resource"
  [url]
  (let [resource (find-or-create {:_id url})]
    (update* resource)))
