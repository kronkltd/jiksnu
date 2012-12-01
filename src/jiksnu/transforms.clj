(ns jiksnu.transforms
  (:require [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model])
  (:import java.net.URI))

(defn set-_id
  [record]
  (if (:_id record)
    record
    (assoc record :_id (model/make-id))))

(defn set-created-time
  [record]
  (if (:created record)
    record
    (assoc record :created (time/now))))

(defn set-updated-time
  [record]
  (if (:updated record)
    record
    (assoc record :updated (time/now))))

(defn set-local
  [item]
  (if (contains? item :local)
    item
    (let [resource (model/get-resource (:url item))]
      (assoc item :local (:local resource)))))

(defn set-domain
  [source]
  (if (:domain source)
    source
    (let [uri (URI. (:url source))
          domain-name (.getHost uri)
          domain (model/get-domain domain-name)]
      (assoc source :domain (:_id domain)))))

(defn set-resource
  [item]
  (if (:resource item)
    item
    (let [resource (model/get-resource (:url item))]
      (assoc item :resource (:_id resource)))))

