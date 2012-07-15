(ns jiksnu.model.feed-source
  (:use [jiksnu.model :only [->FeedSource]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [validation-set presence-of]])
  (:require [clj-time.core :as time]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [monger.collection :as mc]
            [monger.core :as mg])
  (:import jiksnu.model.FeedSource))

(def collection-name "feed_sources")

(def create-validators
  (validation-set
   (presence-of :_id)
   (presence-of :topic)
   (presence-of :subscription-status)
   (presence-of :created)
   (presence-of :updated)
   ))

;; TODO: generalize this and move it to model
(defn set-field!
  "atomically set a field"
  [source key value]
  (mc/update collection-name
             {:topic (:topic source)}
             {:$set {key value}}))

(defn push-value!
  [source key value]
  (mc/update
   collection-name
   {:_id (:_id source)}
   {:$addToSet {key value}}))

(defn fetch-by-id
  [id]
  (if-let [record (mc/find-map-by-id collection-name id)]
    (model/map->FeedSource record)
    (log/warnf "Could not find source with id = %s" id)))

(defn prepare
  [params]
  (let [now (time/now)]
    (merge {:created now
            :updated now
            :_id (model/make-id)
            ;; The initial state of a topic is unknown
            :status "unknown"
            :subscription-status "none"}
           params)))

(defn create
  [params]
  (let [params (prepare params)
        errors (create-validators params)]
    (if (empty? errors)
      (do
        (log/debugf "Creating feed source: %s" params)
        (mc/insert collection-name params)
        ;; TODO: check no errors
        (fetch-by-id (:_id params)))
      (throw+ {:type :validation :errors errors}))))

(defn delete
  "Delete feed source

This will generally not be called"
  [source]
  (log/debugf "Deleting source for %s" (:topic source))
  (mc/remove-by-id collection-name (:_id source))
  source)

(defn find-record
  [options & args]
  (if-let [record (mc/find-one-as-map collection-name options)]
    (model/map->FeedSource record)
    (log/warn "Could not find record")))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (->> (mc/find-maps collection-name params)
          (map model/map->FeedSource))))

(defn fetch-by-topic
  "Fetch a single source by it's topic id"
  [topic]
  (find-record {:topic topic}))

(defn count-records
  ([] (count-records {}))
  ([params]
     (mc/count collection-name params)))
