(ns jiksnu.transforms.resource-transforms
  (:use [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.route-one.core :as r]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model :as model]
            [jiksnu.routes.helpers :as rh])
  (:import java.net.URI))

(defn set-local
  [item]
  (if (contains? item :local)
    item
    (if-let [url (:url item)]
      (if-let [domain-name (.getHost (URI. url))]
        (assoc item :local 
               (= (:_id (actions.domain/current-domain))
                  domain-name))
        (throw+ "Could not determine domain name from url"))
      (throw+ "Could not determine url"))))

(defn set-domain
  [item]
  (if (:domain item)
    item
    (if-let [domain (if (:local item)
                      (actions.domain/current-domain)
                      (when-let [uri (URI. (:url item))]
                        (.getHost uri)))]
      (assoc item :domain (:_id domain))
      (throw+ "Could not determine domain"))))


