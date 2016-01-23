(ns jiksnu.ops
  (:require [jiksnu.channels :as ch]
            jiksnu.model
            [manifold.deferred :as d]
            [manifold.stream :as s]
            [manifold.time :as time]
            [slingshot.slingshot :refer [throw+ try+]]
            [taoensso.timbre :as timbre])
  (:import jiksnu.model.Domain))

;; TODO: Config option
(def default-timeout
  #_(time/minutes 5)
  (time/seconds 30))

(defn channel-description
  [ch]
  (s/description ch)
  "")

(defn op-error
  [ex]
  (if-not (keyword? ex)
    (timbre/errorf "op error: %s" ex)
    ;; FIXME: Handle error

    (timbre/error ex)))

(defn op-success
  [ex]
  #_(timbre/debugf "result realized: %s" (pr-str ex)))

(defn op-handler
  [f]
  (fn [[d args]]
    (try+
      (let [val (apply f args)]
        (d/success! d val))
      (catch Throwable ex
        (timbre/error "op handler error")
        (timbre/error ex)
        (d/error! d ex)))))

(defn async-op
  "Takes a stream and a set of arguments, inserts a deferred and the params, returns the deferred"
  [s args]
  (let [d (d/deferred)]
    (d/timeout! d default-timeout)
    ;; (timbre/debugf "enqueuing #<Channel \"%s\"> << %s"
    ;;             (channel-description d)
    ;;             (pr-str args))
    (s/put! s [d args])
    (d/on-realized d op-success op-error)
    d))

(defn get-conversation
  [url]
  (async-op ch/pending-get-conversation [url]))

(defn get-domain
  [domain-name]
  {:pre [(string? domain-name)]}
  (async-op ch/pending-get-domain [domain-name]))

(defn get-discovered
  [domain & [id options]]
  {:pre [(instance? Domain domain)]}
  (async-op ch/pending-get-discovered [domain id options]))

(defn get-source
  [url]
  (async-op ch/pending-get-source [url]))

(defn get-resource
  [url]
  (async-op ch/pending-get-resource [url]))

(defn update-resource
  [url & [options]]
  (timbre/debug "ops/update-resource")
  (if url
    (async-op ch/pending-update-resources [url options])
    (throw+ "url must not be nil")))

(defn get-user-meta
  [user]
  (async-op ch/pending-get-user-meta [user]))

(defn create-new-conversation
  []
  (timbre/info "creating new conversation")
  (let [d (d/deferred)]
    (d/timeout! d default-timeout)
    (s/put! ch/pending-create-conversations d)
    d))

(defn create-new-stream
  [params]
  (async-op ch/pending-create-stream [params]))

(defn create-new-subscription
  [actor-id user-id]
  (async-op ch/pending-new-subscriptions [actor-id user-id]))
