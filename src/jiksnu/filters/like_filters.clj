(ns jiksnu.filters.like-filters
  (:use (ciste [filters :only (deffilter)])
        jiksnu.actions.like-actions)
  (:require (jiksnu.actions [activity-actions :as actions.activity])))

(deffilter #'like-activity :http
  [action request]
  (-> request :params :id actions.activity/show action))

