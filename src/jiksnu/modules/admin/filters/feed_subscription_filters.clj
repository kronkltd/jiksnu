(ns jiksnu.modules.admin.filters.feed-subscription-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.admin.feed-subscription-actions :only [index]])
  (:require [jiksnu.model.feed-subscription :as model.feed-subscription]))

(deffilter #'index :http
  [action request]
  (action))

;; (deffilter #'show :http
;;   [action request]
;;   (-> request :params :id model.feed-source/fetch-by-id action))

;; (deffilter #'delete :http
;;   [action request]
;;   (-> request :params :id model.feed-source/fetch-by-id action))
