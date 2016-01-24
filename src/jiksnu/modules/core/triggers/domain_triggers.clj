(ns jiksnu.modules.core.triggers.domain-triggers
  (:require [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.service-actions :as actions.service]
            [jiksnu.channels :as ch]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.ops :as ops]
            [manifold.bus :as bus]
            [manifold.stream :as s]
            [taoensso.timbre :as timbre]))

(defn- handle-pending-get-domain*
  [domain-name]
  (actions.domain/find-or-create {:_id domain-name}))

(defn- handle-pending-get-discovered*
  [domain & [id options]]
  (try
    (actions.service/get-discovered domain id options)
    (catch Exception ex
      (timbre/error "Can't discover" ex))))

(def handle-pending-get-domain     (ops/op-handler handle-pending-get-domain*))
(def handle-pending-get-discovered (ops/op-handler handle-pending-get-discovered*))

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
  (s/consume #'handle-pending-get-domain ch/pending-get-domain)
  (s/consume #'handle-pending-get-discovered ch/pending-get-discovered)
  (s/consume #'handle-add-link (bus/subscribe ch/events ":domains:linkAdded")))

(defonce receivers (init-receivers))
