(ns jiksnu.actions.feed-source-actions
  (:use [ciste.config :only [config]]
        [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?>]]
        [clojurewerkz.route-one.core :only [named-path named-url]]
        [jiksnu.session :only [current-user]]
        [lamina.executor :only [task]]
        [slingshot.slingshot :only [throw+]])
  (:require [aleph.http :as http]
            [ciste.model :as cm]
            [clj-http.client :as client]
            [clj-time.core :as time]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.channels :as ch]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.ops :as ops]
            [jiksnu.session :as session]
            [jiksnu.templates :as templates]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.feed-source-transforms :as transforms.feed-source]
            [jiksnu.util :as util]
            [lamina.core :as l])
  (:import java.net.URI
           jiksnu.model.FeedSource))

(defonce pending-discovers (ref {}))

(def discovery-timeout 5000)

(defn get-hub-link
  [feed]
  (-?> feed
       (.getLink "hub")
       .getHref str))

(defn prepare-create
  [source]
  (-> source
      transforms/set-_id
      transforms/set-updated-time
      transforms/set-created-time
      transforms.feed-source/set-domain
      transforms.feed-source/set-local
      transforms.feed-source/set-hub
      transforms.feed-source/set-status
      transforms.feed-source/set-resource))

(defaction add-watcher
  [source user]
  #_(model.feed-source/push-value! source :watchers (:_id user))
  (model.feed-source/fetch-by-id (:_id source)))

(defaction watch
  [source]
  (add-watcher source (session/current-user)))

(defaction delete
  [source]
  (model.feed-source/delete source))

(defaction confirm-subscribe
  "Callback for when a remote subscription has been confirmed"
  [source]
  (model.feed-source/set-field! source :status "confirmed"))

(defaction confirm-unsubscribe
  [source]
  (log/info "confirming subscription removal")
  (model.feed-source/set-field! source :status "none")
  #_(model.feed-source/delete source))

(defaction process-updates
  "Handler for PuSh subscription"
  [params]
  (let [{challenge "hub.challenge"
         mode "hub.mode"
         topic "hub.topic"} params
         source (model.feed-source/fetch-by-topic topic)
         dispatch-fn (condp = mode
                       "subscribe"   #'confirm-subscribe
                       "unsubscribe" #'confirm-unsubscribe
                       (throw+ "Unknown mode"))]
    (dispatch-fn source)
    challenge))

(defaction create
  "Create a new feed source record"
  [params options]
  (let [params (prepare-create params)]
    (model.feed-source/create params)))

(defn find-or-create
  [params & [options]]
  (if-let [source (or (if-let [id (:_id params)]
                        (model.feed-source/fetch-by-id id))
                      (if-let [topic (:topic params)]
                        (model.feed-source/fetch-by-topic topic)))]
    source
    (create params options)))

(def index*
  (templates/make-indexer 'jiksnu.model.feed-source
                          :sort-clause {:created -1}))

(defaction index
  [& options]
  (apply index* options))

(defn mark-updated
  [source]
  (model.feed-source/set-field! source :updated (time/now)))

(declare unsubscribe)
(declare send-unsubscribe)

(defn process-entry
  [[feed source entry]]
  (let [params (actions.activity/entry->activity entry feed source)]
    (actions.activity/find-or-create params)))

(defn parse-feed
  [source feed]
  ;; TODO: use watched? fn
  (if (or true (seq (:watchers source)))
    (doseq [entry (.getEntries feed)]
      (l/enqueue ch/pending-entries [feed source entry]))
    (do (log/warnf "no watchers for %s" (:topic source))
        (unsubscribe source))))

(defn process-feed
  [source feed]
  (let [feed-title (.getTitle feed)
        author (abdera/get-feed-author feed)]
    (when author
      (log/spy (actions.user/person->user author))
      )
    (when-not (= feed-title (:title source))
      (model.feed-source/set-field! source :title feed-title))
    ;; TODO: This should be automatic for any transformation
    (mark-updated source)
    (if-let [hub-link (get-hub-link feed)]
      (model.feed-source/set-field! source :hub hub-link))
    (parse-feed source feed)))

(defaction unsubscribe
  "Action if user makes action to unsubscribe from remote source"
  [source]
  (send-unsubscribe
   (:hub source)
   (:topic source))
  true)

(defn send-unsubscribe
  "Send an unsubscription request to the source's hub"
  ([hub topic]
     (send-unsubscribe hub topic (named-url "push callback")))
  ([hub topic callback]
     (log/debugf "Sending unsubscribe to %s" topic)
     (when (seq hub)
       (client/post
        hub
        {:throw-exceptions false
         :form-params
         {"hub.callback" callback
          "hub.mode" "unsubscribe"
          "hub.topic" topic
          "hub.verify" "async"}}))))

(defn send-subscribe
  [source]
  (if-let [hub (:hub source)]
    (client/post
     hub
     {:throw-exceptions false
      :form-params
      {"hub.callback" (named-url "push callback")
       "hub.mode" "subscribe"
       "hub.topic" (:topic source)
       "hub.verify" "async"}})
    (throw+ "could not find hub")))

(defaction show
  [item]
  item)

(defaction remove-watcher
  [source user]
  #_(model.feed-source/update
      (select-keys source [:_id])
      {:$pull {:watchers (:_id user)}})
  (model.feed-source/fetch-by-id (:_id source)))

(defn update*
  [source]
  (if-not (:local source)
    (if-let [topic (:topic source)]
      (let [resource (ops/get-resource topic)]
        (let [response (actions.resource/update* resource)]
          (if-let [feed (abdera/parse-xml-string (:body response))]
            (process-feed source feed)
            (throw+ "could not obtain feed")))))
    (log/warn "local sources do not need updates")))

(defaction update
  "Fetch updates for the source"
  [source]
  (task
   (try
     (update* source)
     (catch RuntimeException ex
       (log/error ex)
       (.printStackTrace ex))))
  source)

(defaction subscribe
  "Send a subscription request to the feed"
  [source]
  (when-not (:local source)
    (update source)
    (send-subscribe source))
  source)

(defn discover-source
  "determines the feed source associated with a url"
  [url]
  (let [resource (ops/get-resource url)
        response (actions.resource/update* resource)
        body (actions.resource/response->tree response)
        links (actions.resource/get-links body)]
    (if-let [link (util/find-atom-link links)]
      (find-or-create {:topic link})
      (throw+ (format "Could not determine topic url from resource: %s" url)))))

(defaction discover
  [item]
  (update* item)
  item)

(defn get-discovered
  "Returns a copy of that domain once it's properly discovered"
  [item]
  (let [item (find-or-create item)]
    (if (:discovered item)
      item
      (let [id (:_id item)
            p (dosync
               (when-not (get @pending-discovers id)
                 (let [p (promise)]
                   (alter pending-discovers #(assoc % id p))
                   p)))
            p (if p
                (do (discover item) p)
                (get @pending-discovers id))]
        (or (deref p discovery-timeout nil)
            (throw+ "Could not discover feed source"))))))

(defn handle-pending-get-source
  [[p url]]
  (deliver p (find-or-create {:topic url})))

(l/receive-all ch/pending-get-source handle-pending-get-source)
(l/receive-all ch/pending-entries process-entry)

(definitializer
  (model.feed-source/ensure-indexes)

  (require-namespaces
   ["jiksnu.filters.feed-source-filters"
    "jiksnu.triggers.feed-source-triggers"
    "jiksnu.views.feed-source-views"]))
