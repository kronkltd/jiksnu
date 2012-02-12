(ns jiksnu.model.feed-source
  (:use (ciste [debug :only [spy]]))
  (:require (clojure [string :as string])
            (jiksnu [model :as model])
            (karras [entity :as entity]
                    [sugar :as sugar]))
  (:import jiksnu.model.FeedSource))

(defn create
  [options]
  (let [now (sugar/date)]
    (entity/create
     FeedSource
     (merge
      {:created now
       :update now}
      options))))

(defn fetch
  [options]
  (entity/fetch-one FeedSource options))

(defn find-or-create
  [options]
  (or (fetch options)
      (create options)))

(defn index
  []
  (entity/fetch FeedSource {}))
