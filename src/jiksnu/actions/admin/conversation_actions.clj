(ns jiksnu.actions.admin.conversation-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.model :only [implement]]
        [ciste.runner :only [require-namespaces]])
  (:require [jiksnu.actions.conversation-actions :as actions.conversation]))

(defaction index
  [options]
  (actions.conversation/index options))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.conversation-filters"
    "jiksnu.views.admin.conversation-views"]))
