(ns jiksnu.modules.core
  (:require [ciste.event :as event]
            [ciste.loader :refer [defmodule]]
            [clojurewerkz.eep.emitter :refer [defobserver delete-handler get-handler]]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.group-membership-actions :as actions.group-membership]
            [jiksnu.actions.like-actions :as actions.like]
            [jiksnu.actions.notification-actions :as actions.notification]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.channels :as ch]
            [jiksnu.db :as db]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.core.formats
            [jiksnu.modules.core.triggers.domain-triggers :as triggers.domain]
            jiksnu.modules.core.views
            [jiksnu.templates.model :as templates.model]
            [jiksnu.registry :as registry]
            [jiksnu.util :as util]
            [manifold.bus :as bus]
            [manifold.stream :as s]
            [taoensso.timbre :as timbre])
  (:import kamon.Kamon
           kamon.trace.Tracer))

(defn handle-created
  [{:keys [collection-name event item] :as data}]
  ;; (timbre/debugf "%s(%s)=>%s" collection-name (:_id item) event)
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
      (timbre/error "Error in handle-created" ex))))

(defn start
  []
  (timbre/info "Starting core")

  (try
    (Kamon/start)
    (catch Exception _))

  (let [tracer (.newContext (Kamon/tracer) "foo")
        segment (.startSegment (Tracer/currentContext) "set-database" "buisness-logic" "kamon")]
    (db/set-database!)
    (.finish segment)
    ;; (model.activity/ensure-indexes)
    (model.feed-source/ensure-indexes)
    (model.user/ensure-indexes)

    ;; cascade delete on domain deletion
    (dosync
     (alter actions.user/delete-hooks conj #'actions.activity/handle-delete-hook))

    (actions.subscription/setup-delete-hooks)

    (->> (bus/subscribe ch/events :activity-posted)
         (s/consume actions.subscription/handle-follow-activity))

    (->> (bus/subscribe ch/events :activity-posted)
         (s/consume actions.like/handle-like-activity))

    (triggers.domain/init-receivers)

    ;; (timbre/info "setting up handle created")
    (when-not ((get-handler event/emitter) ::templates.model/item-created)
      (defobserver event/emitter ::templates.model/item-created handle-created))

    ;; (defobserver event/emitter ::templates.model/item-created
    ;;   (fn [{:keys [collection-name event item]}]
    ;;     (timbre/info "Duplicate observer")
    ;;     (util/inspect event)))

    (doseq [model-name registry/action-group-names]
      (util/require-module "jiksnu.modules" "core" model-name))

    (.finish tracer)))

(defn stop
  []
  ;; (timbre/info "Stopping core")
  (delete-handler event/emitter ::templates.model/item-created)
  ;; (dosync
  ;;  (ref-set db/_db nil)
  ;;  (ref-set db/_conn nil))
  (Kamon/shutdown))

(def module
  {:name "jiksnu.modules.core"
   :deps []})

(defmodule "jiksnu.modules.core"
  :start start
  :deps [])
