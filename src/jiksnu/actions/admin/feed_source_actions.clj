(ns jiksnu.actions.admin.feed-source-actions
  (:use (ciste [core :only [defaction]]
               [config :only [definitializer]]))
  (:require (jiksnu [model :as model])))

(defn index
  []
  
  []
  )

(defaction show
  [source]
  ;; TODO: look up the source
  true)

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
