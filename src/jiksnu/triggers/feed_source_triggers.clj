(ns jiksnu.triggers.feed-source-triggers
  (:use [ciste.initializer :only [definitializer]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.channels :as ch]
            [jiksnu.ops :as ops]
            [lamina.core :as l]))

(defn handle-pending-get-source
  [url]
  (actions.feed-source/find-or-create {:topic url}))

(defn init-receivers
  []
  (log/info "init receivers")
  (l/receive-all ch/pending-get-source (ops/op-handler handle-pending-get-source))
  (l/receive-all ch/pending-entries actions.feed-source/process-entry))

(defonce receivers (init-receivers))
