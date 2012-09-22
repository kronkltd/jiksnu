(ns jiksnu.filters.conversation-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.conversation-actions)
  (:require [jiksnu.model.conversation :as model.conversation]))

;; create

(deffilter #'create :http
  [action request]
  (-> request :params action))

;; delete

(deffilter #'delete :http
  [action request]
  (-> request :params :id model.conversation/fetch-by-id action))

;; index

(deffilter #'index :http
  [action request]
  (-> request :params action))

;; show

(deffilter #'show :http
  [action request]
  (-> request :params :id model.conversation/fetch-by-id action))

