(ns jiksnu.model.feed-source
  (:use [ciste.debug :only [spy]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [karras.entity :as entity]
            [karras.sugar :as sugar])
  (:import jiksnu.model.FeedSource))

;; TODO: generalize this and move it to model
(defn update-field!
  "atomically set a field"
  [source key value]
  (entity/find-and-modify
   FeedSource
   {:id (:_id source)}
   {:$set {key value}}))

(defn create
  [options]
  (let [now (sugar/date)
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
        (entity/create FeedSource params))
      (throw (RuntimeException. "Source must contain a topic")))))

(defn delete
  "Delete feed source

This will generally not be called"
  [source]
  (log/debugf "Deleting source for %s" (:topic source))
  (entity/delete source)
  source)

;; TODO: rename find-record
(defn fetch
  [options & args]
  (apply entity/fetch-one FeedSource options args))

;; DEPRICATED
(defn find-or-create
  [options]
  (or (fetch options)
      (create options)))

(defn fetch-all
  [options & args]
  (apply entity/fetch FeedSource options args))

(defn fetch-by-id
  [id]
  (entity/fetch-by-id FeedSource id))

(defn fetch-by-topic
  "Fetch a single source by it's topic id"
  [topic]
  (fetch {:topic topic}))
