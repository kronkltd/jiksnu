(ns jiksnu.modules.core.actions.feed-source-actions
  (:require [clj-http.client :as client]
            [clj-time.core :as time]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.ops :as ops]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.feed-source-transforms :as transforms.feed-source]
            [jiksnu.util :as util]
            [manifold.time :as lt]
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre])
  (:import jiksnu.model.FeedSource
           jiksnu.model.User))

(def model-ns 'jiksnu.model.feed-source)

(defonce pending-discovers (ref {}))

;; TODO: Config option
(def discovery-timeout (lt/seconds 30))

(defn prepare-create
  [source]
  (-> source
      transforms/set-_id
      transforms/set-updated-time
      transforms/set-created-time
      transforms.feed-source/set-domain
      transforms.feed-source/set-local
      ;; transforms.feed-source/set-hub
      transforms.feed-source/set-status
      ;; transforms.feed-source/set-resource
      transforms/set-no-links))

(def index*
  (templates.actions/make-indexer model-ns
                                  :sort-clause {:created -1}))

(defn add-watcher
  [^FeedSource source ^User user]
  ;; {:pre [(instance? FeedSource source)
  ;;        (instance? User user)]
  ;;  :post [(instance? FeedSource %)]}
  (model.feed-source/push-value! source :watchers (:_id user))
  (model.feed-source/fetch-by-id (:_id source)))

(defn watch
  [source]
  ;; {:pre [(instance? FeedSource source)]}
  (add-watcher source (session/current-user)))

(defn delete
  [source]
  (model.feed-source/delete source))

(defn confirm-subscribe
  "Callback for when a remote subscription has been confirmed"
  [source]
  (model.feed-source/set-field! source :status "confirmed"))

(defn confirm-unsubscribe
  [source]
  (timbre/info "confirming subscription removal")
  (model.feed-source/set-field! source :status "none")
  #_(model.feed-source/delete source))

(defn process-updates
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

(defn create
  "Create a new feed source record"
  ([params]
   (create params nil))
  ([params options]
   (let [params (prepare-create params)]
     (model.feed-source/create params))))

(defn find-or-create
  [params & [options]]
  (or (if-let [id (:_id params)]
        (model.feed-source/fetch-by-id id))
      (if-let [topic (:topic params)]
        (model.feed-source/fetch-by-topic topic))
      (create params options)))

(defn find-by-resource
  [resource]
  (model.feed-source/fetch-all {:topic (:url resource)}))

(defn index
  [& options]
  (apply index* options))

(defn mark-updated
  [source]
  (model.feed-source/set-field! source :updated (time/now)))

(declare unsubscribe)
(declare send-unsubscribe)

(defn watched?
  "Returns true if the source has any watchers"
  [source]
  (seq (:watchers source)))

(defn unsubscribe
  "Action if user makes action to unsubscribe from remote source"
  [source]
  (send-unsubscribe
   (:hub source)
   (:topic source))
  true)

(defn send-unsubscribe
  "Send an unsubscription request to the source's hub"
  ([hub topic]
   (send-unsubscribe hub topic "" #_(named-url "push callback")))
  ([hub topic callback]
   (timbre/debugf "Sending unsubscribe to %s" topic)
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
      {"hub.callback" "" #_(named-url "push callback")
       "hub.mode" "subscribe"
       "hub.topic" (:topic source)
       "hub.verify" "async"}})
    (throw+ "could not find hub")))

(defn show
  [item]
  item)

(defn remove-watcher
  [source user]
  ;; TODO: implement in terms of a push field or just make a new collection
  #_(model.feed-source/update-record
     (select-keys source [:_id])
     {:$pull {:watchers (:_id user)}})
  (model.feed-source/fetch-by-id (:_id source)))

(defn update-record
  "Fetch updates for the source"
  [source & [options]]
  #_(util/safe-task (update* source options))
  source)

(defn subscribe
  "Send a subscription request to the feed"
  [source]
  (when-not (:local source)
    (update-record source)
    (send-subscribe source))
  source)

(defn discover-source
  "determines the feed source associated with a url"
  [url]
  (if-let [response @(ops/update-resource url)]
    (let [body (model.resource/response->tree response)
          links (model.resource/get-links body)]
      (if-let [link (util/find-atom-link links)]
        (find-or-create {:topic link})
        (throw+ (format "Could not determine topic url from resource: %s" url))))))

(defn discover
  [item]
  (update-record item))

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
