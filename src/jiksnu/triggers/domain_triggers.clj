(ns jiksnu.triggers.domain-triggers
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.channels :as ch]
            [jiksnu.ops :as ops]
            [lamina.core :as l]))

(defn- handle-pending-get-domain
  [domain-name]
  (actions.domain/find-or-create {:_id domain-name}))

(defn init-receivers
  []
  (l/receive-all ch/pending-get-domain (ops/op-handler handle-pending-get-domain)))

(defonce receivers (init-receivers))
