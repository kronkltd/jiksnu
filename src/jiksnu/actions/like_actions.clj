(ns jiksnu.actions.like-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]])
        (jiksnu model session))
  (:require (jiksnu.model [like :as model.like])
            (karras [entity :as entity]
                    [sugar :as sugar]))
  (:import jiksnu.model.Like))

(defn admin-index
  [request]
  {:body
   [:div
    
    ]})


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
  (doseq [namespace ['jiksnu.filters.like-filters
                     'jiksnu.views.like-views]]
    (require namespace)))
