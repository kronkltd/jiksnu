(ns jiksnu.filters.admin.like-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.admin.like-actions :only [delete index]])
  (:require [jiksnu.model :as model]
            [jiksnu.model.like :as model.like]))

(deffilter #'index :http
  [action request]
  ;; TODO: pass page params
  (action))

(deffilter #'delete :http
  [action request]
  (action (-> request :params :id model/make-id model.like/fetch-by-id)))

