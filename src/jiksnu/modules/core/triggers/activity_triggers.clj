(ns jiksnu.modules.core.triggers.activity-triggers
  (:require [taoensso.timbre :as timbre]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.channels :as ch]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.item :as model.item]
            [jiksnu.util :as util]
            [manifold.stream :as s]))

(defn filter-activity-create
  [item]
  (#{#'actions.activity/create}     (:action item)))

(defn create-trigger
  [m]
  (when-let [activity (:records m)]
    (util/inspect activity)
    (let [author (model.activity/get-author activity)]

      ;; Add item to author's stream
      (model.item/push author activity)

      (when-let [id (:conversation activity)]
        (when-let [conversation (model.conversation/fetch-by-id id)]
          (actions.conversation/add-activity conversation activity)))
      )))

(defn init-receivers
  []
  (timbre/info "init receivers")
  (s/consume create-trigger ch/posted-activities)

  ;; Create events for each created activity
  #_(s/connect
     (s/filter filter-activity-create ciste.core/*actions*)
     ch/posted-activities)
  )

(defonce receivers (init-receivers))
