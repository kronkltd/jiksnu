(ns jiksnu.modules.core.triggers.activity-triggers
  (:require [taoensso.timbre :as timbre]
            [jiksnu.model :as model]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.channels :as ch]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.item :as model.item]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.ops :as ops]
            [manifold.stream :as s])
  (:import java.net.URI
           jiksnu.model.User))

(defn filter-activity-create
  [item]
  (#{#'actions.activity/create}     (:action item)))

(defn create-trigger
  [m]
  (if-let [activity (:records m)]
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
