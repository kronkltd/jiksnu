(ns jiksnu.actions.admin.feed-source-actions
  (:use (ciste [config :only [definitializer]]))
  (:require (jiksnu [model :as model])))

(defn index
  []
  
  []
  )

(definitializer
  ;; (try
  (doseq [namespace [
                     'jiksnu.filters.admin.feed-source-filters
                     ;; 'jiksnu.helpers.admin.feed-source-helpers
                     ;; 'jiksnu.sections.admin.feed-source-sections
                     ;; 'jiksnu.triggers.admin.feed-source-triggers
                     'jiksnu.views.admin.feed-source-views
                     ]]
      (require namespace))
    ;; (catch Exception ex))
  )
