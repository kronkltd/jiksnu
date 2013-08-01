(ns jiksnu.filters.like-filters
  (:use [ciste.filters :only [deffilter]]
        [clojure.core.incubator :only [-?>]]
        jiksnu.actions.like-actions)
  (:require [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.like :as model.like]
            [jiksnu.session :as session]
            [jiksnu.util :as util]))

(deffilter #'like-activity :http
  [action request]
  (let [actor (session/current-user)
        activity (-?> request :params :id model.activity/fetch-by-id)]
    (action activity actor)))

(deffilter #'index :http
  [action request]
  (-> request :params action))

(deffilter #'delete :http
  [action request]
  (action (-> request :params :id util/make-id model.like/fetch-by-id)))
