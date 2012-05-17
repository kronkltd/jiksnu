(ns jiksnu.actions.like-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.runner :only [require-namespaces]])
  (:require [jiksnu.model.like :as model.like]
            [karras.entity :as entity]
            [karras.sugar :as sugar])
  (:import jiksnu.model.Like))

(defn admin-index
  [request]
  (model.like/fetch-all {} :limit 20))

(defaction like-activity
  [activity-id user-id]
  (model.like/create
   {:user user-id
    :activity activity-id
    :created (sugar/date)}))

(defn get-likes
  [activity]
  (entity/fetch Like {:activity (:_id activity)}))

(definitializer
  (require-namespaces
   ["jiksnu.filters.like-filters"
    "jiksnu.views.like-views"]))
