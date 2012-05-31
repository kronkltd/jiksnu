(ns jiksnu.actions.message-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]
               [runner :only [require-namespaces]])))

(defaction inbox-page
  [user]
  [user []]
  )

(defaction outbox-page
  [user]
  [user []]

  )

(definitializer
  (require-namespaces
   ["jiksnu.filters.activity-filters"
    "jiksnu.sections.activity-sections"
    "jiksnu.triggers.activity-triggers"
    "jiksnu.views.activity-views"]))
