(ns jiksnu.actions.feed-source-actions
  (:use [ciste.config :only [config]]
        [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.model :only [implement]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?>]]
        [slingshot.slingshot :only [throw+]])
  (:require [aleph.http :as http]
            [ciste.model :as cm]
            [clj-http.client :as client]
            [clj-time.core :as time]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.helpers.user-helpers :as helpers.user]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [lamina.core :as l])
  (:import java.net.URI
           jiksnu.model.FeedSource))

(defonce
  ^{:doc "Channel containing list of sources to be updated"}
  pending-updates (l/permanent-channel))

(defn mark-updated
  [source]
  (model.feed-source/set-field! source :updated (time/now)))

(defaction delete
  [source]
  (model.feed-source/delete source))

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
        "subscribe" (confirm source)

        "unsubscribe" (do
                        (log/info "confirming subscription removal")
                        (model.feed-source/set-field! source :subscription-status "none")
                        #_(model.feed-source/delete source))
       (cm/implement
        (log/warn "Unknown mode"))))
    challenge))

(defaction create
  "Create a new feed source record"
  [params options]
  (if-let [topic (:topic params)]
    (let [uri (URI. topic)
          domain (actions.domain/find-or-create {:_id (.getHost uri)})]
      (model.feed-source/create (assoc params
                                  :domain (:_id domain))))
    (throw+ "Must contain a topic")))

(def index*
  (model/make-indexer 'jiksnu.model.feed-source
                      :sort-clause [{:_id 1}]))

(defaction index
  [& options]
  (apply index* options))


(defn find-or-create
  [params & [options & _]]
  (if-let [source (or (and (:_id params) (model.feed-source/fetch-by-id (:_id params)))
                      (model.feed-source/fetch-by-topic (:topic params)))]
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
     (log/debugf "Sending unsubscribe to %s" topic)
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
  [source]
  (send-unsubscribe
   (:hub source)
   (:topic source))
  true)

(defn get-activities
  "extract the activities from a feed"
  [feed]
  (map #(actions.activity/entry->activity % feed)
       (.getEntries feed)))

(defn process-entries
  [feed]
  (doseq [activity (get-activities feed)]
    (try (actions.activity/find-or-create activity)
         (catch Exception ex
           (log/error ex)))))

(defaction fetch-updates
  "Fetch updates for the source"
  [source]
  (let [{:keys [topic]} source]
    (log/debugf "Fetching feed: %s" topic)
    (let [feed (abdera/fetch-feed topic)
          feed-title (.getTitle feed)]
      (when-not (= feed-title (:title source))
        (model.feed-source/set-field! source :title feed-title))
      (mark-updated source)
      (if-let [hub-link (-?> feed (.getLink "hub")
                             .getHref str)]
        (model.feed-source/set-field! source :hub hub-link))
      (process-entries feed)
      source)))

(defaction add-watcher
  [source user]
  (model.feed-source/push-value! source :watchers (:_id user))
  (model.feed-source/fetch-by-id (:_id source)))

(defaction remove-watcher
  [source user]
  (model.feed-source/update
   (select-keys source [:_id])
   {:$pull {:watchers (:_id user)}})
  (model.feed-source/fetch-by-id (:_id source)))

(definitializer
  (require-namespaces
   [
    "jiksnu.filters.feed-source-filters"
    "jiksnu.triggers.feed-source-triggers"
    "jiksnu.views.feed-source-views"]))
