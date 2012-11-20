(ns jiksnu.transforms.feed-source-transforms
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model :as model])
  (:import java.net.URI))

(defn set-resource
  [item]
  (if (:resource item)
    item
    (let [resource (model/get-resource (:topic item))]
      (assoc item :resource (:_id resource)))))

(defn set-domain
  [source]
  (if (:domain source)
    source
    (let [uri (URI. (:topic source))
          domain (actions.domain/get-discovered {:_id (.getHost uri)})]
      (assoc source :domain (:_id domain)))))

(defn set-status
  [item]
  (if (:status item)
    item
    (assoc item :status "none")))
