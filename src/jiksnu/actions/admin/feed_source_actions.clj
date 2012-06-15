(ns jiksnu.actions.admin.feed-source-actions
  (:use [ciste.core :only [defaction]]
        [ciste.config :only [definitializer]]
        [ciste.runner :only [require-namespaces]])
  (:require [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]))

(def index*
  (model/make-indexer 'jiksnu.model.feed-source))

(defaction index
  [& options]
  (apply index* options))

(defn delete
  [source]
  (actions.feed-source/delete source))

(defaction show
  [source]
  source)

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.feed-source-filters"
    "jiksnu.sections.feed-source-sections"
    "jiksnu.views.admin.feed-source-views"]))
