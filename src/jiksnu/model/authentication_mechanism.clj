(ns jiksnu.model.authentication-mechanism
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.templates :as templates]
            [jiksnu.util :as util]
            [monger.core :as mg]
            [monger.collection :as mc])
  (:import jiksnu.model.AuthenticationMechanism))

(def collection-name "authentication_mechanisms")

(def set-field! (templates/make-set-field! collection-name))

(defn create
  [options]
  (log/debugf "creating auth mechanism: %s" options)
  (mc/insert collection-name options))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (map model/map->AuthenticationMechanism
          (mc/find-maps collection-name params))))

(defn fetch-by-user
  [user & options]
  (apply fetch-all {:user (:_id user)} options))

(def delete        (templates/make-deleter collection-name))
(def drop!         (templates/make-dropper collection-name))
(def count-records (templates/make-counter collection-name))

(defn fetch-by-id
  [id]
  (let [id (if (string? id) (util/make-id id) id)]
    (if-let [item (mc/find-map-by-id collection-name id)]
      (model/map->AuthenticationMechanism item))))

