(ns jiksnu.transforms.feed-source-transforms
  (:require [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.ops :as ops]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]]))

(defn set-domain
  [source]
  (if (:domain source)
    source
    (let [domain-name (util/get-domain-name (:topic source))
          domain @(ops/get-discovered @(ops/get-domain domain-name))]
      (assoc source :domain (:_id domain)))))

(defn set-status
  [item]
  (if (:status item)
    item
    (assoc item :status "none")))

(defn set-local
  [item]
  (if (:local item)
    item
    (if-let [domain (actions.domain/find-or-create {:_id (:domain item)})]
      (assoc item :local (:local domain))
      (throw+ "Could not determine domain"))))
