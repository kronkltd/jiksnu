(ns jiksnu.model.feed-subscription
  (:require [clj-time.core :as time]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [monger.collection :as mc]
            [monger.core :as mg])
  (:import jiksnu.model.FeedSubscription))

(def collection-name "feed_subscriptions")

(defn create
  [options]
  (let [now (time/now)]
    (mc/insert collection-name
               (merge
                {:created now
                 :update now}
                options))))

(defn fetch-all
  [options]
  (model/map->FeedSubscription (mc/find-one-as-map collection-name options)))
