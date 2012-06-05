(ns jiksnu.actions.admin.activity-actions
  "This is the namespace for the admin pages for activities"
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.runner :only [require-namespaces]])
  (:require [jiksnu.model.activity :as model.activity]))

(defaction index
  [options]
  (model.activity/fetch-all {} {:page 1}))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.activity-filters"
    ;; "jiksnu.helpers.admin.activity-helpers"
    ;; "jiksnu.sections.admin.activity-sections"
    ;; "jiksnu.triggers.admin.activity-triggers"
    "jiksnu.views.admin.activity-views"]))
