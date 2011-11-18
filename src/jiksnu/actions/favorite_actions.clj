(ns jiksnu.actions.favorite-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]])))

(defaction user-list
  [user]
  ;; TODO: implement
  [])

(definitializer
  (doseq [namespace [
                     'jiksnu.filters.favorite-filters
                     ;; 'jiksnu.helpers.favorite-helpers
                     ;; 'jiksnu.sections.favorite-sections
                     ;; 'jiksnu.triggers.favorite-triggers
                     'jiksnu.views.favorite-views]]
    (require namespace)))
