(ns jiksnu.transforms
  (:use [ciste.config :only [config]]
        [slingshot.slingshot :only [throw+]])
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
    (if-let [url (:url item)]
      (if-let [domain-name (util/get-domain-name url)]
        (assoc item :local
               (= (config :domain) domain-name))
        (throw+ "Could not determine domain name from url"))
      (throw+ "Could not determine url"))))

(defn set-domain
  [item]
  (if (:domain item)
    item
    (let [domain-name (util/get-domain-name (:url item))
          domain (ops/get-domain domain-name)]
      (assoc item :domain (:_id @domain)))))

(defn set-resource
  [item]
  (if (:resource item)
    item
    (let [resource (ops/get-resource (:url item))]
      (assoc item :resource (:_id @resource)))))

(defn set-no-links
  [item]
  (if (seq (:links item))
    (throw+ "Can not create item with links. Create record then add links")
    item))
