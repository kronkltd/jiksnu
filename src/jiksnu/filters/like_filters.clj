(ns jiksnu.filters.like-filters
  (:use (ciste [debug :only [spy]]
               [filters :only [deffilter]])
        (clojure.core [incubator :only [-?>]])
        (jiksnu [session :only [current-user-id]])
        jiksnu.actions.like-actions)
  (:require (jiksnu.actions [activity-actions :as actions.activity])
            (jiksnu.model [activity :as model.activity])))

(deffilter #'like-activity :http
  [action request]
  (let [user-id (current-user-id)
        activity-id (-?>  request :params spy :id model.activity/fetch-by-id spy :_id)]
    (action activity-id user-id)))
