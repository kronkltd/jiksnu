(ns jiksnu.actions.tag-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]
               [runner :only [require-namespaces]]))
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
  (require-namespaces
   ["jiksnu.filters.tag-filters"
    "jiksnu.views.tag-views"]))
