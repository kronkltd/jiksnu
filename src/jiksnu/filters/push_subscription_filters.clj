(ns jiksnu.filters.push-subscription-filters
  (:use (ciste [debug :only (spy)]
               [filters :only (deffilter)])
        jiksnu.actions.push-subscription-actions)
  (:require (jiksnu [abdera :as abdera])
            (jiksnu.model [user :as model.user])))

(deffilter #'callback :http
  [action request]
  (action request))

(deffilter #'callback-publish :http
  [action request]
  (action request))

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'hub :http
  [action request]
  (action (:params request)))

(deffilter #'hub-publish :http
  [action request]
  (action (:params request)))

(deffilter #'subscribe :http
  [action request]
  (-> request :params :id
      model.user/fetch-by-id action))
