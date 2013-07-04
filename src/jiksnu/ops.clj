(ns jiksnu.ops
  (:use [slingshot.slingshot :only [throw+]])
  (:require [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.channels :as ch]
            [lamina.core :as l]
            [lamina.time :as time]
            [lamina.trace :as trace]))

;; TODO: Config option
(def default-timeout
  #_(time/minutes 5)
  (time/seconds 30))

(defn channel-description
  [ch]
  (lamina.core.utils/description (lamina.core.channel/receiver-node ch)))

(defn op-error
  [ex]
  (log/errorf "op error: %s" ex)
  (trace/trace :errors:handled ex))

(defn op-success
  [ex]
  (log/infof "result realized: %s" (pr-str ex)))

(defn op-handler
  [f]
  (fn [[result args]]
    (try
      (let [val (apply f args)]
        (l/enqueue result val))
      (catch RuntimeException ex
        (l/error result ex)))))

(defn async-op
  [ch args]
  (let [result (l/expiring-result default-timeout)]
    (log/debugf "enqueuing #<Channel \"%s\"> << %s" (channel-description ch) (pr-str args))
    (l/enqueue ch [result args])
    (l/on-realized result op-success op-error)
    result))

(defn create-new-conversation
  []
  (s/increment "conversations create new")
  (let [result (l/expiring-result default-timeout)]
    (l/enqueue ch/pending-create-conversations result)
    #_(l/wait-for-result result default-timeout)
    result))

(defn get-conversation
  [url]
  (async-op ch/pending-get-conversation [url]))

(defn get-domain
  [domain-name]
  (async-op ch/pending-get-domain [domain-name]))

(defn get-discovered
  [domain]
  (async-op ch/pending-get-discovered [domain]))

(defn get-source
  [url]
  (async-op ch/pending-get-source [url]))

(defn get-resource
  [url]
  (async-op ch/pending-get-resource [url]))

(defn update-resource
  [url & [options]]
  (if url
    (async-op ch/pending-update-resources [url options])
    (throw+ "url must not be nil")))

(defn get-user-meta
  [user]
  (async-op ch/pending-get-user-meta [user]))

(defn create-new-subscription
  [actor-id user-id]
  (async-op ch/pending-new-subscriptions [actor-id user-id]))
