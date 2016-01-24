(ns jiksnu.actions.subscription-actions
  (:require [ciste.initializer :refer [definitializer]]
            [clojure.string :as string]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.channels :as ch]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.util :as util]
            [manifold.bus :as bus]
            [manifold.stream :as s]
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre]))

(defonce delete-hooks (ref []))

(defn prepare-delete
  ([item]
   (prepare-delete item @delete-hooks))
  ([item hooks]
   (if (seq hooks)
     (recur ((first hooks) item) (rest hooks))
     item)))

(defn prepare-create
  [user]
  (-> user
      transforms/set-_id
      transforms/set-updated-time
      transforms/set-created-time))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.subscription))

(defn create
  [params]
  (let [params (prepare-create params)]
    (model.subscription/create params)))

(defn delete
  "Deletes a subscription.

   This action is primarily for the admin console.
   In most cases, use the user-specific versions. (unsubscribe)"
  [subscription]
  (model.subscription/delete subscription))

(defn index
  [& options]
  (apply index* options))

(defn ostatussub
  [profile]
  ;; TODO: Allow for http uri's
  (if profile
    (let [[username domain] (string/split profile #"@")]
      (model.user/get-user username domain))
    (model/map->User {})))

(defn show
  [item]
  item)

(defn subscribe
  [actor user]
  ;; Set up a feed source to that user's public feed
  (when-not (:local user)
    (actions.user/discover user)

    (let [user (model.user/fetch-by-id (:_id user))]

      (actions.user/update-record user {})

      (if-let [source-id (:update-source user)]
        (when-let [source (model.feed-source/fetch-by-id source-id)]
          (actions.feed-source/add-watcher source actor))
        (timbre/warn "Could not find source"))))
  (create {:from (:_id actor)
           :to (:_id user)
           :local true
           :pending true}))

(defn unsubscribed
  [actor user]
  (let [subscription (model.subscription/find-record
                      {:from (:_id actor)
                       :to (:_id user)})]
    (model.subscription/delete subscription)
    subscription))

(defn ostatussub-submit
  "User requests a subscription to a uri"
  [uri]
  (if-let [actor (session/current-user)]
    (if-let [user (actions.user/find-or-create {:_id uri})]
      (subscribe actor user)
      (throw+ {:type :validation :message "Could not determine user"}))
    (throw+ {:type :authentication :message "must be logged in"})))

(defn subscribed
  [actor user]
  (create
   {:from (:_id actor)
    :to (:_id user)
    :local false}))

(defn get-subscribers
  [user]
  [user (index {:to (:_id user)})])

(defn get-subscriptions
  [user]
  [user (index {:from (:_id user)})])

(defn unsubscribe
  "User unsubscribes from another user"
  [actor target]
  (if-let [subscription (model.subscription/find-by-users actor target)]
    (model.subscription/unsubscribe actor target)
    (throw+ "Subscription not found")))

(defn confirm
  [subscription]
  (model.subscription/confirm subscription))

(defn setup-delete-hooks*
  [user]
  (let [subscriptions (concat
                       (:items (second (get-subscribers user)))
                       (:items (second (get-subscriptions user))))]
    (doseq [subscription subscriptions]
      (delete subscription))
    user))

(defn setup-delete-hooks
  []
  (dosync
   (alter actions.user/delete-hooks
          conj setup-delete-hooks*)))

(defn handle-follow-activity
  [activity]
  (let [{:keys [verb]} activity]
    (condp = verb
      "follow"
      (do (timbre/info "follow action")
          (let [actor (model.user/fetch-by-id (:author activity))
                target (model.user/fetch-by-id (:id (:object activity)))]
            (subscribe actor target)))
      "unfollow"
      (do (timbre/info "follow action")
          (let [actor (model.user/fetch-by-id (:author activity))
                target (model.user/fetch-by-id (:id (:object activity)))]
            (unsubscribe actor target)))
      nil)))

(definitializer

  #_(bus/publish! ch/events :activity-posted {:msg "activity posted"})

  (setup-delete-hooks)

  (->> (bus/subscribe ch/events :activity-posted)
       (s/consume handle-follow-activity)))
