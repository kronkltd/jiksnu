(ns jiksnu.triggers.conversation-triggers
  (:use [ciste.initializer :only [definitializer]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.channels :as ch]
            [jiksnu.ops :as ops]
            [lamina.core :as l])
  )

(defn- handle-get-conversation
  [url]
  (actions.conversation/find-or-create {:url url}))

(defn- enqueue-create-local
  [ch]
  (l/enqueue ch (actions.conversation/create {:local true})))

(defn init-handlers
  []
  (l/receive-all ch/pending-get-conversation (ops/op-handler handle-get-conversation))
  (l/receive-all ch/pending-create-conversations enqueue-create-local))

(definitializer
  (init-handlers))
