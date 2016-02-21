(ns jiksnu.modules.core
  (:require [ciste.event :as event]
            [ciste.loader :refer [defmodule]]
            [clojurewerkz.eep.emitter :refer [defobserver delete-handler get-handler]]
            [jiksnu.actions.stream-actions :as actions.stream]
            jiksnu.modules.core.formats
            [jiksnu.modules.core.triggers.domain-triggers :as triggers.domain]
            jiksnu.modules.core.views
            [jiksnu.templates.model :as templates.model]
            [jiksnu.db :as db]
            [jiksnu.registry :as registry]
            [jiksnu.util :as util]
            [taoensso.timbre :as timbre]))

(def handlers (ref []))

(defn start
  []
  (db/set-database!)

  (triggers.domain/init-receivers)

  (let [emitter
        (defobserver event/emitter ::templates.model/item-created
          (fn [{:keys [collection-name event item]}]
            (condp = collection-name

              "activities"
              (do
                (timbre/info "activity created")
                (condp = (:verb item)
                  (do
                    (timbre/info "Unknown verb")
                    (util/inspect item))))

              "users"
              (do
                (timbre/info "user created")
                (actions.stream/add-stream (util/inspect item) "* major")
                (actions.stream/add-stream item "* minor"))

              (do
                (timbre/infof "Other created - %s" collection-name)
                #_(util/inspect item)))))]

    (dosync
     (alter handlers conj emitter)))

  ;; (defobserver event/emitter ::templates.model/item-created
  ;;   (fn [{:keys [collection-name event item]}]
  ;;     (timbre/info "Duplicate observer")
  ;;     (util/inspect event)))

  (doseq [model-name registry/action-group-names]
    (util/require-module "jiksnu.modules" "core" model-name)))

(defn stop
  []
  (timbre/info "Stopping core")

  (get-handler event/emitter ::templates.model/item-created)
  (delete-handler event/emitter ::templates.model/item-created)
  (get-handler event/emitter ::templates.model/item-created))

(def module
  {:name "jiksnu.modules.core"
   :deps []})

(defmodule "jiksnu.modules.core"
  :start start
  :deps [])
