(ns jiksnu.actions.group-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]))
  (:require (jiksnu.model [group :as model.group])))

(defaction new-page
  []
  true)

(defaction index
  []
  (model.group/index))

(defaction user-list
  [user]
  ;; TODO: implement
  [])

(defaction add
  [name]
  (model.group/create {:name name}))

(definitializer
  (doseq [namespace ['jiksnu.filters.group-filters
                     ;; 'jiksnu.helpers.group-helpers
                     'jiksnu.views.group-views]]
    (require namespace)))
