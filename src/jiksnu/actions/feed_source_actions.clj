(ns jiksnu.actions.feed-source-actions
  (:use [ciste.config :only [config definitializer]]
        [ciste.core :only [defaction]]
        [ciste.debug :only [spy]]
        [ciste.model :only [implement]]
        [ciste.runner :only [require-namespaces]]
        [karras.entity :only [make]])
  (:require [aleph.http :as http]
            [ciste.model :as cm]
            [clj-http.client :as client]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.helpers.user-helpers :as helpers.user]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [karras.sugar :as sugar]
            [lamina.core :as l])
  (:import jiksnu.model.FeedSource))

(defonce
  ^{:doc "Channel containing list of sources to be updated"}
  pending-updates (l/permanent-channel))

(defn mark-updated
  [source]
  (model.feed-source/set-field! source :updated (sugar/date)))

(defaction confirm
  "Callback for when a remote subscription has been confirmed"
  [source]
  (model.feed-source/set-field! source :subscription-status "confirmed"))

(defaction process-updates
  "Handler for PuSh subscription"
  [params]
  (let [{challenge "hub.challenge"
         mode "hub.mode"
         topic "hub.topic"} params]
    (let [source (model.feed-source/fetch-by-topic topic)]
      (condp = mode
        "subscribe" (do
                      (implement (log/info "confirming subscription")))

        "unsubscribe" (do
                        (implement (log/info "confirming subscription removal"))
                        ;; TODO: don't delete, just make
                        ;; subscription as canceled.
                        (model.feed-source/delete source))
       (cm/implement
        (log/warn "Unknown mode"))))
    challenge))

(defaction create
  "Create a new feed source record"
  [params options]
  (spy options)
  (model.feed-source/create params))

(defn find-or-create
  [params options]
  (if-let [source (or (and (:_id params) (model.feed-source/fetch-by-id (:_id params)))
                      (model.feed-source/fetch-by-topic {:topic params}))]
    source
    (create params options)))

;; TODO: special case local subscriptions
;; TODO: should take a source
(defaction subscribe
  "Send a subscription request to the feed"
  [user]
  (if-let [hub-url (:hub user)]
    (let [topic (helpers.user/feed-link-uri user)]
      (find-or-create {:topic topic :hub hub-url} {})
      (client/post
       hub-url
       {:throw-exceptions false
        :form-params
        {"hub.callback" (str "http://" (config :domain) "/main/push/callback")
         "hub.mode" "subscribe"
         "hub.topic" topic
         "hub.verify" "async"}}))))

(defn send-unsubscribe
  "Send an unsubscription request to the source's hub"
  ([hub topic]
     (send-unsubscribe
      hub topic
      ;; TODO: This should be a per-source callback url
      (str "http://" (config :domain) "/main/push/callback")))
  ([hub topic callback]
     (client/post
      hub
      {:throw-exceptions false
       :form-params
       {"hub.callback" callback
        "hub.mode" "unsubscribe"
        "hub.topic" topic
        "hub.verify" "async"}})))

;; TODO: Rename to unsubscribe and make an action
(defaction remove-subscription
  "Action if user makes action to unsubscribe from remote source"
  [subscription]
  (send-unsubscribe
   (:hub subscription)
   (:topic subscription))
  true)

(defaction fetch-updates
  "Fetch updates for the source"
  [source]
  (let [{:keys [topic]} source]
    (log/debug (str "Fetching feed: " topic))
    (let [feed (abdera/fetch-feed topic)]
      (mark-updated source)
      feed)))

(definitializer
  (require-namespaces
   [
    "jiksnu.filters.feed-source-filters"
    "jiksnu.views.feed-source-views"]))
