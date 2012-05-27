(ns jiksnu.actions.admin.like-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.model :only [implement]]
        [ciste.runner :only [require-namespaces]])
  (:require [jiksnu.actions.like-actions :as actions.like]))

(defaction index
  [options]
  (actions.like/index options))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.like-filters"
    "jiksnu.views.admin.like-views"]))
