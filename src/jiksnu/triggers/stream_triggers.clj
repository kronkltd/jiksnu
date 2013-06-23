(ns jiksnu.triggers.stream-triggers
  (:use [ciste.initializer :only [definitializer]])
  (:require [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.channels :as ch]
            [lamina.core :as l]))

(defn filter-activity-create
  [item]
  (#{#'actions.activity/create}     (:action item)))

(defn filter-conversation-create
  [item]
  (#{#'actions.conversation/create} (:action item)))

(defn init-receivers
  []

  ;; Create events for each created activity
  (l/siphon
   (l/filter* filter-activity-create (l/fork ciste.core/*actions*))
   ch/posted-activities)

  ;; Create events for each created conversation
  (l/siphon
   (l/filter* filter-conversation-create (l/fork ciste.core/*actions*))
   ch/posted-conversations)
  )

(defonce receivers (init-receivers))

