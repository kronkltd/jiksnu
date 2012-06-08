(ns jiksnu.model.feed-source
  (:use [jiksnu.model :only [->FeedSource]])
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
   (presence-of :updated)))

;; TODO: generalize this and move it to model
(defn set-field!
  "atomically set a field"
  [source key value]
  (mc/update collection-name
             {:topic (:topic source)}
             {:$set {key value}}))

(defn create
  [options]
  (let [now (time/now)
        params (merge {:created now
                       ;; The initial state of a topic is unknown
                       :status "unknown"
                       :subscription-status "none"}
                      options)]
    (let [errors (create-validators params)]
      (if (empty? errors)
        (do
          (log/debugf "Creating feed source for %s" (:topic params))
          (mc/insert collection-name params))
        (throw+ {:type :validation :errors errors})))))

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
    (model/map->FeedSource record)))

(defn fetch-all
  [& args]
  (->> (mc/find-maps collection-name args)
       (map model/map->FeedSource)))

(defn fetch-by-id
  [id]
  (if-let [record (mc/find-map-by-id collection-name id)]
    (model/map->FeedSource record)
    (log/warnf "Could not find source with id = %s" id)))

(defn fetch-by-topic
  "Fetch a single source by it's topic id"
  [topic]
  (find-record {:topic topic}))

(defn count-records
  ([] (count-records {}))
  ([params]
     (mc/count collection-name params)))
