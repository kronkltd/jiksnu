(ns jiksnu.modules.core
  (:require [ciste.event :as event]
            [ciste.loader :refer [defmodule]]
            [clojurewerkz.eep.emitter :refer [defobserver delete-handler get-handler]]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.like-actions :as actions.like]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.channels :as ch]
            [jiksnu.db :as db]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.core.formats
            [jiksnu.modules.core.triggers.activity-triggers :as triggers.activity]
            [jiksnu.modules.core.triggers.conversation-triggers :as triggers.conversation]
            [jiksnu.modules.core.triggers.domain-triggers :as triggers.domain]
            jiksnu.modules.core.views
            [jiksnu.templates.model :as templates.model]
            [jiksnu.registry :as registry]
            [jiksnu.util :as util]
            [manifold.bus :as bus]
            [manifold.stream :as s]
            [taoensso.timbre :as timbre]))

(defn start
  []
  (db/set-database!)
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

  (defobserver event/emitter ::templates.model/item-created
    (fn [{:keys [collection-name event item]}]
      (condp = collection-name

        "activities"
        (let [verb (:verb item)]
          #_(timbre/info "activity created")
          (condp = verb
            (do
              "post"
              (do
                #_(timbre/debug "activity posted"))

              #_(timbre/infof "Unknown verb - %s" verb)
              #_(util/inspect item))))

        "users"
        (do
          #_(timbre/info "user created")
          (actions.stream/add-stream item "* major")
          (actions.stream/add-stream item "* minor"))

        (do
          #_(timbre/infof "Other created - %s" collection-name)
          #_(util/inspect item)))))

  ;; (defobserver event/emitter ::templates.model/item-created
  ;;   (fn [{:keys [collection-name event item]}]
  ;;     (timbre/info "Duplicate observer")
  ;;     (util/inspect event)))

  (doseq [model-name registry/action-group-names]
    (util/require-module "jiksnu.modules" "core" model-name)))

(defn stop
  []
  (timbre/info "Stopping core")
  (delete-handler event/emitter ::templates.model/item-created))

(def module
  {:name "jiksnu.modules.core"
   :deps []})

(defmodule "jiksnu.modules.core"
  :start start
  :deps [])
