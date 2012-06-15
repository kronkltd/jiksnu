(ns jiksnu.actions.admin.feed-subscription-actions
  (:use [ciste.core :only [defaction]]
        [ciste.config :only [definitializer]]
        [ciste.model :only [implement]]
        [ciste.runner :only [require-namespaces]])
  (:require [jiksnu.model :as model]
            [jiksnu.model.feed-subscription :as model.feed-subscription]))

(def index*
  (model/make-indexer 'jiksnu.model.feed-subscription))

(defaction index
  [& options]
  (apply index* options))

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
