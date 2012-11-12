(ns jiksnu.actions.message-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]])
  (:require [ciste.model :as cm]))

(defaction inbox-page
  [user]
  (cm/implement
      [user []]))

(defaction outbox-page
  [user]
  (cm/implement
      [user []]))

(definitializer
  (require-namespaces
   ["jiksnu.filters.activity-filters"
    "jiksnu.sections.activity-sections"
    "jiksnu.triggers.activity-triggers"
    "jiksnu.views.activity-views"]))
