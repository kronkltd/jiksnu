(ns jiksnu.actions.admin.auth-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.runner :only [require-namespaces]])
  (:require [jiksnu.model :as model]
            [jiksnu.model.authentication-mechanism :as model.authentication-mechanism]))

(def index*
  (model/make-indexer 'jiksnu.model.authentication-mechanism))

(defaction index
  [& options]
  (apply index* options))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.auth-filters"
    "jiksnu.views.admin.auth-views"]))
