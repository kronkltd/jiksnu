(ns jiksnu.actions.admin.key-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.model :only [implement]]
        [ciste.loader :only [require-namespaces]])
  (:require [jiksnu.actions.key-actions :as actions.key]))

(defaction create
  [options]
  (actions.key/create options))

(defaction delete
  [options]
  (actions.key/delete options))

(defaction show
  [options]
  (actions.key/show options))

(defaction index
  [options]
  (actions.key/index options))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.key-filters"
    "jiksnu.views.admin.key-views"]))
