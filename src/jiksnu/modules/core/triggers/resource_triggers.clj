(ns jiksnu.modules.core.triggers.resource-triggers
  (:use [slingshot.slingshot :only [throw+]])
  (:require [taoensso.timbre :as timbre]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.channels :as ch]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.ops :as ops]
            [manifold.stream :as s]))

(defn handle-alternate-link
  [item link]
  (condp = (:type link)
    "application/atom+xml" (let [source (ops/get-source (:href link))]
                             (model.resource/set-field! item :updateSource (:_id source))
                             #_(actions.feed-source/update-record source))
    nil))

(defn add-link-trigger
  [m]
  (let [[item link] (:args m)]
    (condp = (:rel link)
      "alternate" (handle-alternate-link item link)
      nil)))

(defn handle-pending-get-resource*
  [url]
  (actions.resource/find-or-create {:_id url}))

(defn handle-pending-update-resources*
  [url & [options]]
  (when-let [resource (actions.resource/find-or-create {:_id url})]
    (try @(actions.resource/update* resource options)
         (catch Exception ex
           (timbre/error "update resource error")))))

(def handle-pending-get-resource     (ops/op-handler handle-pending-get-resource*))
(def handle-pending-update-resources (ops/op-handler handle-pending-update-resources*))

(defn init-receivers
  []
  ;; (s/consume add-link-trigger ch/resource-links-added)
  (s/consume #'handle-pending-get-resource ch/pending-get-resource)
  (s/consume #'handle-pending-update-resources ch/pending-update-resources))

(defonce receivers (init-receivers))
