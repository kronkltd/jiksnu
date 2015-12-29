(ns jiksnu.actions.oauth-actions
  (:require [crypto.random :as random]))

(defn request-token
  []
  (random/base32 20))

(defn authorize
  []
  true)

(defn access-token
  []
  true)
