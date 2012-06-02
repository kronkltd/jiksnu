(ns jiksnu.model.feed-source
  (:require [clj-time.core :as time]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [monger.collection :as mc]
            [monger.core :as mg])
  (:import jiksnu.model.FeedSource))

(def collection-name "feed_sources")

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
        params (merge
                {:created now
                 ;; The initial state of a topic is unknown
                 :status "unknown"
                 :subscription-status "none"
                 ;; :update now
                 }
                options)]
    (if (:topic params)
      ;; TODO: topic must be a valid url
      ;; TODO: topic must have a discovered domain
      (do
        (log/debugf "Creating feed source for %s" (:topic params))
        (mc/insert collection-name params))
      (throw (RuntimeException. "Source must contain a topic")))))

(defn delete
  "Delete feed source

This will generally not be called"
  [source]
  (log/debugf "Deleting source for %s" (:topic source))
  (mc/remove collection-name source)
  source)

(defn find-record
  [options & args]
  (->FeedSource (mc/find-one-as-map collection-name options)))

(defn fetch-all
  [& args]
  (map ->FeedSource (mc/find-maps collection-name args)))

(defn fetch-by-id
  [id]
  (mc/find-map-by-id collection-name id))

(defn fetch-by-topic
  "Fetch a single source by it's topic id"
  [topic]
  (fetch {:topic topic}))

(defn count-records
  ([] (count-records {}))
  ([params]
     (mc/count collection-name params)))
