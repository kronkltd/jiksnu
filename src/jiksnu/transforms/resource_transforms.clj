(ns jiksnu.transforms.resource-transforms
  (:use [ciste.config :only [config]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.ops :as ops]
            [jiksnu.util :as util]))

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
    (if-let [domain-name (if (:local item)
                           (config :domain)
                           (util/get-domain-name (:url item)))]
      (assoc item :domain domain-name)
      (throw+ "Could not determine domain"))))

(defn set-location
  [item]
  (if-let [location (:location item)]
    ;; TODO: handle this as a content handler
    (let [resource (ops/get-resource location)]
      (ops/update-resource @resource)
      item)
    item))
