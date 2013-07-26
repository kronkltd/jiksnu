(ns jiksnu.actions.admin.auth-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]])
  (:require [jiksnu.model :as model]
            [jiksnu.model.authentication-mechanism :as model.authentication-mechanism]
            [jiksnu.templates.actions :as templates.actions]))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.authentication-mechanism))

(defaction index
  [& options]
  (apply index* options))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.auth-filters"
    "jiksnu.views.admin.auth-views"]))
