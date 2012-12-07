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
(def default-timeout (time/minutes 5))

(defn async-op
  [ch params]
  (let [p (promise)
        description (lamina.core.utils/description (lamina.core.channel/receiver-node ch))]
    (log/debugf "enqueuing #<Channel \"%s\"> << %s" description (pr-str params))
    (l/enqueue ch [p params])
    (or (deref p default-timeout nil)
        (throw+ "timeout"))))

(defn create-new-conversation
  []
  (s/increment "conversations create new")
  (let [result (l/result-channel)]
    (l/enqueue ch/pending-create-conversations result)
    (l/wait-for-result result default-timeout)))

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
