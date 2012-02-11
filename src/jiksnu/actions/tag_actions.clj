(ns jiksnu.actions.tag-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]))
  (:require (jiksnu.model [activity :as model.activity])
            (karras [collection :as coll]
                    [core :as karras]
                    [sugar :as sugar])))

(defaction index
  []
  [])

(defaction show
  [tag]
  [tag
   (model.activity/fetch-all
    {:tags tag
     :public true}
    :limit 20
    :sort [(sugar/desc :published)])])

(definitializer
  (doseq [namespace ['jiksnu.filters.tag-filters
                     ;; 'jiksnu.helpers.tag-helpers
                     ;; 'jiksnu.sections.tag-sections
                     ;; 'jiksnu.triggers.tag-triggers
                     'jiksnu.views.tag-views
                     ]]
    (require namespace)))
