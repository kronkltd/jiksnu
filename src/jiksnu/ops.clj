(ns jiksnu.ops
  (:use [slingshot.slingshot :only [throw+]])
  (:require [clj-statsd :as s]
            [clojure.string :as string]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [inflections.core :as inf]
            [jiksnu.channels :as ch]
            [jiksnu.db :as db]
            [jiksnu.namespace :as ns]
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
  (log/errorf "op error: " ex)
  (log/spy ex))

(defn op-success
  [ex]
  (log/infof "result realized: " ex)
  (log/spy ex))

(defn async-op
  [ch params]
  (let [result (l/expiring-result default-timeout)]
    (log/debugf "enqueuing #<Channel \"%s\"> << %s" (channel-description ch) (pr-str params))
    (l/enqueue ch [result params])
    (l/on-realized result
                   op-success
                   op-error)
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
  (async-op ch/pending-get-conversation url))

(defn get-domain
  [domain-name]
  (async-op ch/pending-get-domain domain-name))

(defn get-discovered
  [domain]
  (async-op ch/pending-get-discovered domain))

(defn get-source
  [url]
  (async-op ch/pending-get-source url))

(defn get-resource
  [url]
  (async-op ch/pending-get-resource url))


(defn update-resource
  [resource]
  (async-op ch/pending-update-resources resource))

(defn get-user-meta
  [user]
  (async-op ch/pending-get-user-meta user))
