(ns
    ^{:doc "This is the namespace for the admin pages for activities"}
  jiksnu.actions.admin.activity-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]))
  (:require (jiksnu.model [activity :as model.activity])))

(defaction index
  []
  (model.activity/index))

(definitializer
  ;; (try
  (doseq [namespace [
                     'jiksnu.filters.admin.activity-filters
                     ;; 'jiksnu.helpers.admin.activity-helpers
                     ;; 'jiksnu.sections.admin.activity-sections
                     ;; 'jiksnu.triggers.admin.activity-triggers
                     'jiksnu.views.admin.activity-views
                     ]]
    (require namespace))
  ;; (catch Exception ex))
  )
