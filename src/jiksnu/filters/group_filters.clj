(ns jiksnu.filters.group-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.group-actions)
  (:require [jiksnu.model.group :as model.group]
            [jiksnu.model.user :as model.user]))

(deffilter #'add :http
  [action request]
  (-> request :params action))

(deffilter #'create :http
  [action request]
  (-> request :params action))

(deffilter #'edit-page :http
  [action request]
  (action (model.group/fetch-by-name (:name (:params request)))))

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'new-page :http
  [action request]
  (action))

(deffilter #'user-list :http
  [action request]
  (-> request :params :id
      model.user/fetch-by-id action))
