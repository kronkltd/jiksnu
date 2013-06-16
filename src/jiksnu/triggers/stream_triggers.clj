(ns jiksnu.triggers.stream-triggers
  (:use [ciste.initializer :only [definitializer]])
  (:require ciste.core
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.channels :as ch]
                     [jiksnu.ops :as ops]
   [lamina.core :as l]
         ))

(defn init-receivers
  []

  ;; Create events for each created activity
  (l/siphon
   (->> ciste.core/*actions*
        l/fork
        (l/filter* (comp #{#'actions.activity/create} :action)))
   ch/posted-activities)
  (l/receive-all ch/posted-activities identity)

  ;; Create events for each created conversation
  (l/siphon
   (->> ciste.core/*actions*
        l/fork
        (l/filter* (comp #{#'actions.conversation/create} :action)))
   ch/posted-conversations)
  (l/receive-all ch/posted-conversations identity))

(definitializer
  (init-receivers)
  )
