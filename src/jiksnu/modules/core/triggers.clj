(ns jiksnu.modules.core.triggers
  (:require [ciste.core :refer [with-context]]
            [ciste.event :as event]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.actions.group-membership-actions :as actions.group-membership]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.like-actions :as actions.like]
            [jiksnu.actions.notification-actions :as actions.notification]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.actions.service-actions :as actions.service]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.channels :as ch]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.group-membership :as model.group-membership]
            [jiksnu.model.item :as model.item]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.ops :as ops]
            [jiksnu.templates.model :as templates.model]
            [jiksnu.util :as util]
            [manifold.bus :as bus]
            [manifold.deferred :as d]
            [manifold.stream :as s]
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre])
  (:import kamon.Kamon
           kamon.trace.Tracer))

(defn filter-activity-create
  [item]
  (#{#'actions.activity/create}     (:action item)))

(defn filter-conversation-create
  [item]
  (#{#'actions.conversation/create} (:action item)))

;; TODO: make op handler
(defn- enqueue-create-local
  [d]
  (d/success! d (actions.conversation/create {:local true})))

(defn parse-avatar
  [user link]
  (when (= (first (:extensions link)) "96")
    (model.user/set-field! user :avatarUrl (:href link))))

(defn parse-updates-from
  [user link]
  (timbre/debug "Setting update source")
  (if-let [href (:href link)]
    (let [source (actions.feed-source/find-or-create {:topic href})]
      (model.user/set-field! user :update-source (:_id source)))
    (throw+ "link must have a href")))

(defn parse-activity-outbox
  [user link]
  (timbre/debug "Setting update source")
  (if-let [href (:href link)]
    (let [source (actions.feed-source/find-or-create {:topic href})]
      (model.user/set-field! user :update-source (:_id source)))
    (throw+ "link must have a href")))

(defn- handle-get-conversation*
  [url]
  (actions.conversation/find-or-create {:url url}))

(defn- handle-pending-get-domain*
  [domain-name]
  (actions.domain/find-or-create {:_id domain-name}))

(defn- handle-pending-get-discovered*
  [domain & [id options]]
  (try
    (actions.service/get-discovered domain id options)
    (catch Exception ex
      (timbre/error ex "Can't discover"))))

(defn- handle-pending-get-resource*
  [url]
  (actions.resource/find-or-create {:_id url}))

(defn- handle-pending-get-user-meta*
  [user]
  (actions.user/get-user-meta user))

(defn- handle-pending-new-subscriptions*
  [actor-id user-id]
  (let [actor (model.user/fetch-by-id actor-id)
        user (model.user/fetch-by-id user-id)]
    (actions.subscription/subscribe actor user)))

(defn- handle-pending-update-resources*
  [url & [options]]
  (when-let [resource (actions.resource/find-or-create {:_id url})]
    (try @(actions.resource/update* resource options)
         (catch Exception ex
           (timbre/error ex "update resource error")))))

(defn handle-alternate-link
  [item link]
  (condp = (:type link)
    "application/atom+xml" (let [source (ops/get-source (:href link))]
                             (model.resource/set-field! item :updateSource (:_id source))
                             #_(actions.feed-source/update-record source))
    nil))

(defn add-link-trigger
  [m]
  (let [[item link] (:args m)]
    (condp = (:rel link)
      "alternate" (handle-alternate-link item link)
      nil)))

;; (defn fetch-updates-trigger
;;   [action _ user]
;;   (let [domain (model.user/get-domain user)]
;;     (when (:xmpp domain) (fetch-updates-xmpp user))
;;     #_(fetch-updates-http user)))

(defn handle-add-domain-link
  [[item link]]
  (condp = (:rel link)
    "lrdd" (condp = (:type link)
             "application/xrd+xml" (model.domain/set-field! item :xrdTemplate (:template link))
             "application/json"    (model.domain/set-field! item :jrdTemplate (:template link))
             nil)
    nil))

(defn handle-add-user-link
  [[user link]]
  (condp = (:rel link)
    ;; "magic-public-key" (parse-magic-public-key user link)
    "avatar"          (parse-avatar user link)
    "activity-outbox" (parse-activity-outbox user link)
    ns/updates-from   (parse-updates-from user link)
    nil))

(defn handle-create-activity
  [m]
  (when-let [activity (:records m)]
    (let [author (model.activity/get-author activity)]

      ;; Add item to author's stream
      (model.item/push author activity)

      (when-let [id (:conversation activity)]
        (when-let [conversation (model.conversation/fetch-by-id id)]
          (actions.conversation/add-activity conversation activity))))))

(defn handle-created
  [{:keys [collection-name event item] :as data}]
  (timbre/debugf "%s(%s)=>%s" collection-name (:_id item) event)
  (.increment (.counter (Kamon/metrics) "records-created"))
  (try
    (condp = collection-name
      "activities" (when (= (:verb item) "join")
                     (let [object-id (get-in item [:object :id])
                           group (model.group/fetch-by-id object-id)]
                       (actions.group-membership/create
                        {:user (:author item)
                         :group (:_id group)})))

      "likes" (do
                (actions.notification/create {:user (:user item)
                                              :activity (:activity item)}))

      "users" (do (actions.stream/add-stream item "* major")
                  (actions.stream/add-stream item "* minor"))

      nil)
    (catch Exception ex
      (timbre/error ex "Error in handle-created"))))

(def handle-get-conversation          (ops/op-handler handle-get-conversation*))
(def handle-pending-get-domain        (ops/op-handler handle-pending-get-domain*))
(def handle-pending-get-discovered    (ops/op-handler handle-pending-get-discovered*))
(def handle-pending-get-resource      (ops/op-handler handle-pending-get-resource*))
(def handle-pending-get-user-meta     (ops/op-handler handle-pending-get-user-meta*))
(def handle-pending-new-subscriptions (ops/op-handler handle-pending-new-subscriptions*))
(def handle-pending-update-resources  (ops/op-handler handle-pending-update-resources*))

(defn bind-handlers!
  []

  #_
  (s/consume add-link-trigger                            ch/resource-links-added)

  (s/consume handle-get-conversation                     ch/pending-get-conversation)
  (s/consume enqueue-create-local                        ch/pending-create-conversations)
  (s/consume #'handle-create-activity                    ch/posted-activities)
  (s/consume #'handle-pending-get-domain                 ch/pending-get-domain)
  (s/consume #'handle-pending-get-discovered             ch/pending-get-discovered)
  (s/consume #'handle-pending-get-resource               ch/pending-get-resource)
  (s/consume #'handle-pending-update-resources           ch/pending-update-resources)
  (s/consume handle-pending-new-subscriptions            ch/pending-new-subscriptions)
  (s/consume #'handle-pending-get-user-meta              ch/pending-get-user-meta)
  (s/consume #'handle-add-domain-link                    (bus/subscribe event/events ":domains:linkAdded"))
  (s/consume #'handle-add-user-link                      (bus/subscribe event/events ":users:linkAdded"))
  (s/consume handle-created                              (bus/subscribe event/events ::templates.model/item-created))
  (s/consume actions.subscription/handle-follow-activity (bus/subscribe event/events :activity-posted))
  (s/consume actions.like/handle-like-activity           (bus/subscribe event/events :activity-posted))

  (util/add-hook!
   actions.domain/delete-hooks
   (fn [domain]
     (doseq [user (:items (model.user/fetch-by-domain domain))]
       (actions.user/delete user))
     domain))

  ;; Create events for each created conversation
  ;; TODO: listen to trace probe
  #_
  (s/connect
   (s/filter filter-conversation-create ciste.core/*actions*)
   ch/posted-conversations)

  ;; Create events for each created activity
  #_
  (s/connect
   (s/filter filter-activity-create ciste.core/*actions*)
   ch/posted-activities)


  ;; cascade delete on domain deletion
  #_
  (dosync
   (alter actions.user/delete-hooks conj #'actions.activity/handle-delete-hook))

  #_
  (actions.subscription/setup-delete-hooks))
