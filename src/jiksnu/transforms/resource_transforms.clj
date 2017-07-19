(ns jiksnu.transforms.resource-transforms
  (:require [ciste.config :refer [config]]
            [jiksnu.modules.core.ops :as ops]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]]))

(defn set-local
  [item]
  (if (contains? item :local)
    item
    (if-let [url (:_id item)]
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
                           (util/get-domain-name (:_id item)))]
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
