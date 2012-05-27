(ns jiksnu.actions.admin.user-actions
  "This is the namespace for the admin pages for users"
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.runner :only [require-namespaces]])
  (:require [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.user :as model.user]))

(defaction create
  [options]
  (actions.user/create options))

(defaction index
  [options]
  (actions.user/index options))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.user-filters"
    "jiksnu.views.admin.user-views"]))
