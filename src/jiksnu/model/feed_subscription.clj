(ns jiksnu.model.push-subscription
  (:use (ciste [debug :only [spy]]))
  (:require (clojure [string :as string])
            (karras [entity :as entity]
                    [sugar :as sugar]))
  (:import jiksnu.model.FeedSubscription))

(defn create
  [options]
  (let [now (sugar/date)]
    (entity/create
     FeedSubscription
     (merge
      {:created now
       :update now}
      options))))

(defn fetch
  [options]
  (entity/fetch-one FeedSubscription options))

(defn find-or-create
  [options]
  (or (fetch options)
      (create options)))

(defn index
  []
  (entity/fetch FeedSubscription {}))
