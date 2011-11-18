(ns jiksnu.filters.favorite-filters
  (:use (ciste [filters :only [deffilter]])
        jiksnu.actions.favorite-actions)
  (:require (jiksnu.model [user :as model.user])))

(deffilter #'user-list :http
  [action request]
  (-> request :params :id model.user/fetch-by-id action))
