(ns jiksnu.transforms
  (:require [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [jiksnu.ops :as ops]
            [jiksnu.util :as util])
  (:import java.net.URI))

(defn set-_id
  [item]
  (if (:_id item)
    item
    (assoc item :_id (util/make-id))))

(defn set-created-time
  [item]
  (if (:created item)
    item
    (assoc item :created (time/now))))

(defn set-updated-time
  [item]
  (if (:updated item)
    item
    (assoc item :updated (time/now))))

(defn set-local
  [item]
  (if (contains? item :local)
    item
    (let [resource (ops/get-resource (:url item))]
      (assoc item :local (:local @resource)))))

(defn set-domain
  [item]
  (if (:domain item)
    item
    (let [uri (URI. (:url item))
          domain-name (.getHost uri)
          domain (ops/get-domain domain-name)]
      (assoc item :domain (:_id @domain)))))

(defn set-resource
  [item]
  (if (:resource item)
    item
    (let [resource (ops/get-resource (:url item))]
      (assoc item :resource (:_id @resource)))))

