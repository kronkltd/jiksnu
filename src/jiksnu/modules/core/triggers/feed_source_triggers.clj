(ns jiksnu.modules.core.triggers.feed-source-triggers
  (:require [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.modules.atom.actions.feed-source-actions :as atom.actions.feed-source]
            [jiksnu.channels :as ch]
            [jiksnu.ops :as ops]
            [lamina.core :as l]
            [lamina.trace :as trace]))

(defn handle-pending-get-source*
  [url]
  (actions.feed-source/find-or-create {:topic url}))

(def handle-pending-get-source
  (ops/op-handler handle-pending-get-source*))

(defn init-receivers
  []
  (l/receive-all ch/pending-get-source
                 handle-pending-get-source)

  (l/receive-all ch/pending-entries
                 atom.actions.feed-source/process-entry)

  (l/receive-all (trace/probe-channel :feeds:processed)
                 (fn [feed]
                   (s/increment "feeds processed")))

  )

(defonce receivers (init-receivers))
