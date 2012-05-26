(ns jiksnu.actions.oauth-actions
  (:use [ciste.core :only [defaction]]
        [ciste.model :only [implement]]))

(defaction oauthapps
  "List registered applications"
  []
  (implement))
