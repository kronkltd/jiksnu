(ns jiksnu.filters.like-filters
  (:use [ciste.filters :only [deffilter]]
        [clojure.core.incubator :only [-?>]]
        [jiksnu.session :only [current-user]]
        jiksnu.actions.like-actions)
  (:require [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.activity :as model.activity]))

(deffilter #'like-activity :http
  [action request]
  (let [actor (current-user)
        activity (-?> request :params :id model.activity/fetch-by-id)]
    (action activity actor)))

(deffilter #'index :http
  [action request]
  (-> request :params action))
