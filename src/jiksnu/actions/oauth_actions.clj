(ns jiksnu.actions.oauth-actions
  (:use [ciste.core :only [defaction]])
  (:require [ciste.model :as cm]
            [crypto.random :as random]))

(defaction oauthapps
  "List registered applications"
  []
  (cm/implement))

(defaction request-token
  []
  (random/base32 20))

(defn authorize
  []

  true
  )

(defn access-token
  []

  true
  )

