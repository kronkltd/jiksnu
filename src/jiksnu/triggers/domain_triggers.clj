(ns jiksnu.triggers.domain-triggers
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.channels :as ch]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.ops :as ops]
            [lamina.core :as l]
            [lamina.trace :as trace]))

(defn- handle-pending-get-domain*
  [domain-name]
  (actions.domain/find-or-create {:_id domain-name}))

(def handle-pending-get-domain (ops/op-handler handle-pending-get-domain*))

(defn handle-add-link
  [[item link]]
  (condp = (:rel link)

    "lrdd"
    (condp = (:type link)

      "application/xrd+xml"
      (model.domain/set-field! item :xrdTemplate (:template link))

      "application/json"
      (model.domain/set-field! item :jrdTemplate (:template link))

      nil)

    nil))

(defn init-receivers
  []

  (l/receive-all ch/pending-get-domain
                 handle-pending-get-domain)

  (l/receive-all (trace/probe-channel :domains:linkAdded)
                 handle-add-link)

  )

(defonce receivers (init-receivers))
