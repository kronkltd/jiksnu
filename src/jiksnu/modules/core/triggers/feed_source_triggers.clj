(ns jiksnu.modules.core.triggers.feed-source-triggers
  (:require [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [lamina.core :as l]
            [lamina.trace :as trace]))

(defn init-receivers
  []
  (l/receive-all (trace/probe-channel :feeds:processed)
                 (fn [feed]
                   (s/increment "feeds processed")))

  )

(defonce receivers (init-receivers))
