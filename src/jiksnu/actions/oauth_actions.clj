(ns jiksnu.actions.oauth-actions
  (:require [ciste.core :refer [defaction]]
            [crypto.random :as random]))

(defaction request-token
  []
  (random/base32 20))

(defn authorize
  []
  true)

(defn access-token
  []
  true)
