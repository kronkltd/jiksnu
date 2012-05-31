(ns jiksnu.actions.group-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]
               [runner :only [require-namespaces]]))
  (:require (jiksnu.model [group :as model.group]))
  (:import jiksnu.model.Group))

(defaction new-page
  []
  (Group.))

(defaction index
  []
  (model.group/index))

(defaction user-list
  [user]
  ;; TODO: implement
  [])

(defaction add
  [params]
  (model.group/create params))

(definitializer
  (require-namespaces
   ["jiksnu.filters.group-filters"
    "jiksnu.views.group-views"]))
