(ns jiksnu.actions.like-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]])
        (jiksnu model session))
  (:require (jiksnu.model [like :as model.like]))
  (:import jiksnu.model.Like))

(definitializer
  (doseq [namespace ['jiksnu.filters.like-filters]]
    (require namespace)))

(defn admin-index
  [request]
  {:body
   [:div
    
    ]})


(defaction like-activity
  [& _])

