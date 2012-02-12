(ns jiksnu.actions.message-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]])))

(defaction inbox-page
  [user]
  [user []]
  )

(defaction outbox-page
  [user]
  [user []]

  )

(definitializer
  (doseq [namespace ['jiksnu.filters.activity-filters
                     'jiksnu.sections.activity-sections
                     'jiksnu.triggers.activity-triggers
                     'jiksnu.views.activity-views]]
    (try (require namespace))))
