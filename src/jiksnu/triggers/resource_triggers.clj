(ns jiksnu.triggers.resource-triggers
  (:use [ciste.initializer :only [definitializer]]
        [ciste.triggers :only [add-trigger!]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.channels :as ch]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.ops :as ops]
            [lamina.core :as l]))

(defn add-link-trigger
  [action [item link] _]
  (condp = (:rel link)
    "alternate" (if (= (:type link) "application/atom+xml")
                  (let [source (ops/get-source (:href link))]
                    (model.resource/set-field! item :updateSource (:_id source))
                    #_(actions.feed-source/update source)))
    nil))

(add-trigger! #'actions.resource/add-link*     #'add-link-trigger)

(defn handle-pending-get-resource
  [url]
  (actions.resource/find-or-create {:url url}))

(defn handle-pending-update-resources
  [item]
  (actions.resource/update* item))

(defn init-receivers
  []
  (l/receive-all ch/pending-get-resource     (ops/op-handler handle-pending-get-resource))
  (l/receive-all ch/pending-update-resources (ops/op-handler handle-pending-update-resources)))

(defonce receivers (init-receivers))
