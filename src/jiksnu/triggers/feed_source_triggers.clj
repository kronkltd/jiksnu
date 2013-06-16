(ns jiksnu.triggers.feed-source-triggers
  (:use [ciste.initializer :only [definitializer]]
        [ciste.triggers :only [add-trigger!]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.channels :as ch]
            [jiksnu.ops :as ops]
            [lamina.core :as l]))


(defn create-trigger
  "This will cause every new source to be updated.

Note. This causes a lot of records to be created"
  [action params source]
  (actions.feed-source/update source))

#_(add-trigger! #'actions.feed-source/create #'create-trigger)


(defn handle-pending-get-source
  [url]
  (actions.feed-source/find-or-create {:topic url}))

(defn init-receivers
  []
  (l/receive-all ch/pending-get-source (ops/op-handler handle-pending-get-source))
  (l/receive-all ch/pending-entries actions.feed-source/process-entry))

(definitializer
  (init-receivers))
