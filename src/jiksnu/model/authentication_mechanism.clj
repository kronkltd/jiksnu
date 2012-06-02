(ns jiksnu.model.authentication-mechanism
  (:require [monger.core :as mg]
            [monger.collection :as mc])
  (:import jiksnu.model.AuthenticationMechanism))

(def collection-name "authentication_mechanisms")

(defn create
  [options]
  (mc/insert collection-name options))

(defn fetch-all
  [& options]
  (map ->AuthenticationMechanism
       (mc/find-maps collection-name options)))
