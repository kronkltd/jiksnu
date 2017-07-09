(ns jiksnu.modules.core.actions.oauth-actions
  (:require [crypto.random :as random]))

(defn request-token
  [request]
  (random/base32 20))

(defn authorize
  [_]
  true)

(defn access-token
  []
  true)
