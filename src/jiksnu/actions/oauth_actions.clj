(ns jiksnu.actions.oauth-actions
  (:use [ciste.core :only [defaction]])
  (:require [ciste.model :as cm]))

(defaction oauthapps
  "List registered applications"
  []
  (cm/implement))
