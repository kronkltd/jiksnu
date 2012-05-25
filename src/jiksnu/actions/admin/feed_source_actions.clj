(ns jiksnu.actions.admin.feed-source-actions
  (:use [ciste.core :only [defaction]]
        [ciste.config :only [definitializer]]
        [ciste.debug :only [spy]]
        [ciste.runner :only [require-namespaces]])
  (:require [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]))

(defaction index
  "List feed sources

for admin use"
  [options]
  [(model.feed-source/fetch-all
    {}
    ;; TODO: hardcoded configurable value
    ;; :limit 20
    ) options])

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
