(ns jiksnu.actions.admin.feed-subscription-actions
  (:use [ciste.core :only [defaction]]
        [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]])
  (:require [ciste.model :as cm]
            [jiksnu.model :as model]
            [jiksnu.model.feed-subscription :as model.feed-subscription]))

(def index*
  (model/make-indexer 'jiksnu.model.feed-subscription))

(defaction index
  [& options]
  (apply index* options))

(defaction delete
  [record]
  (cm/implement))

(defaction show
  [record]
  (cm/implement))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.feed-subscription-filters"
    "jiksnu.sections.feed-subscription-sections"
    "jiksnu.views.admin.feed-subscription-views"]))
