(ns jiksnu.model.feed-source
  (:use (ciste [debug :only [spy]]))
  (:require (clojure [string :as string])
            (jiksnu [model :as model])
            (karras [entity :as entity]
                    [sugar :as sugar]))
  (:import jiksnu.model.FeedSource))

(defn update-field!
  [source key value]
  (entity/find-and-modify
   FeedSource
   {:id (:_id source)}
   {:$set {key value}}))

(defn create
  [options]
  (let [now (sugar/date)]
    (entity/create
     FeedSource
     (merge
      {:created now
       :status "pending"
       :update now}
      options))))

(defn delete
  [source]
  (entity/delete source)
  source)

(defn fetch
  [options & args]
  (apply entity/fetch-one FeedSource options args))

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
