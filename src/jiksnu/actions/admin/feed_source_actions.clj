(ns jiksnu.actions.admin.feed-source-actions
  (:use (ciste [core :only [defaction]]
               [config :only [definitializer]]))
  (:require (jiksnu [model :as model])
            (jiksnu.model [feed-source :as model.feed-source])))

(defaction index
  "List feed sources

for admin use"
  []
  (model.feed-source/fetch-all
   {}
   ;; TODO: hardcoded configurable value
   :limit 20))

(defaction show
  [source]
  source)

(definitializer
  ;; (try
  (doseq [namespace [
                     'jiksnu.filters.admin.feed-source-filters
                     ;; 'jiksnu.helpers.admin.feed-source-helpers
                     'jiksnu.sections.feed-source-sections
                     ;; 'jiksnu.triggers.admin.feed-source-triggers
                     'jiksnu.views.admin.feed-source-views
                     ]]
    (require namespace))
  ;; (catch Exception ex))
  )
