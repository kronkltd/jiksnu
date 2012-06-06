(ns jiksnu.model.authentication-mechanism
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [monger.core :as mg]
            [monger.collection :as mc])
  (:import jiksnu.model.AuthenticationMechanism))

(def collection-name "authentication_mechanisms")

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
