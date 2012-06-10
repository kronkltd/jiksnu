(ns jiksnu.actions.admin.group-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.model :only [implement]]
        [ciste.runner :only [require-namespaces]])
  (:require [jiksnu.actions.group-actions :as actions.group]))

(defaction index
  [options]
  (actions.group/index options))

(defaction create
  [params]
  (actions.group/create params))

(defaction delete
  [group]
  (actions.group/delete group))

(defaction show
  [group]
  (actions.group/show group))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.group-filters"
    "jiksnu.views.admin.group-views"]))
