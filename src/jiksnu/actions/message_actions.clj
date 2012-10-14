(ns jiksnu.actions.message-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.model :only [implement]]
        [ciste.loader :only [require-namespaces]]))

(defaction inbox-page
  [user]
  (implement
      [user []]))

(defaction outbox-page
  [user]
  (implement
      [user []]))

(definitializer
  (require-namespaces
   ["jiksnu.filters.activity-filters"
    "jiksnu.sections.activity-sections"
    "jiksnu.triggers.activity-triggers"
    "jiksnu.views.activity-views"]))
