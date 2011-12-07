(ns jiksnu.filters.group-filters
  (:use (ciste [filters :only [deffilter]])
        jiksnu.actions.group-actions)
  (:require (jiksnu.model [user :as model.user])))

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'user-list :http
  [action request]
  (-> request :params :id
      model.user/fetch-by-id action))

(deffilter #'new-page :http
  [action request]
  (action))

(deffilter #'add :http
  [action request]
  (-> request :params :name action))
