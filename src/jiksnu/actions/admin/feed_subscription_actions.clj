(ns jiksnu.actions.admin.feed-subscription-actions
  (:use [ciste.core :only [defaction]]
        [ciste.config :only [definitializer]]
        [ciste.model :only [implement]]
        [ciste.runner :only [require-namespaces]])
  (:require [jiksnu.model.feed-subscription :as model.feed-subscription]))

(defaction index
  [& [params & [options & _]]]
  (model.feed-subscription/fetch-all params options))

(defaction delete
  [record]
  (implement))

(defaction show
  [record]
  (implement))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.feed-subscription-filters"
    "jiksnu.sections.feed-subscription-sections"
    "jiksnu.views.admin.feed-subscription-views"]))
