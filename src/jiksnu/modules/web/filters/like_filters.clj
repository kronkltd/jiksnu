(ns jiksnu.modules.web.filters.like-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.like-actions :as actions.like]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.like :as model.like]
            [jiksnu.session :as session]))

(deffilter #'actions.like/delete :http
  [action request]
  (action (-> request :params :id model.like/fetch-by-id)))

(deffilter #'actions.like/index :http
  [action request]
  (-> request :params action))

(deffilter #'actions.like/like-activity :http
  [action request]
  (let [actor (session/current-user)
        activity (some-> request :params :id model.activity/fetch-by-id)]
    (action activity actor)))

