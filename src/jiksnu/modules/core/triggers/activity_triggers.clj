(ns jiksnu.modules.core.triggers.activity-triggers
  (:require [ciste.config :refer [config]]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.channels :as ch]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.item :as model.item]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.ops :as ops]
            [lamina.core :as l])
  (:import java.net.URI
           jiksnu.model.Activity
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

  (l/receive-all ch/posted-activities create-trigger)

  ;; Create events for each created activity
  #_(l/siphon
   (l/filter* filter-activity-create (l/fork ciste.core/*actions*))
   ch/posted-activities)

  )

(defonce receivers (init-receivers))
