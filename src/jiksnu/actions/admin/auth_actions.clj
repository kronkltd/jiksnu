(ns jiksnu.actions.admin.auth-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.runner :only [require-namespaces]])
  (:require [jiksnu.model.authentication-mechanism :as model.authentication-mechanism]))

(defaction index
  [& options]
  (model.authentication-mechanism/fetch-all options))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.auth-filters"
    "jiksnu.views.admin.auth-views"]))
