(ns jiksnu.actions.like-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]])
        (jiksnu model session))
  (:require (jiksnu.model [like :as model.like]))
  (:import jiksnu.model.Like))

(defn admin-index
  [request]
  {:body
   [:div
    
    ]})


(defaction like-activity
  [& _])

(definitializer
  (doseq [namespace ['jiksnu.filters.like-filters
                     'jiksnu.views.like-views]]
    (require namespace)))
