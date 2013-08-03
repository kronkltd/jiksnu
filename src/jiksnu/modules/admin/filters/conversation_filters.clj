(ns jiksnu.modules.admin.filters.conversation-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.admin.conversation-actions :only [create index show delete fetch-updates]])
  (:require [jiksnu.model.conversation :as model.conversation]))

(deffilter #'create :http
  [action request]
  ;; TODO: injection
  (-> request :params action))

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'show :http
  [action request]
  (action (-> request :params :id model.conversation/fetch-by-id)))

(deffilter #'delete :http
  [action request]
  (action (-> request :params :id model.conversation/fetch-by-id)))

(deffilter #'fetch-updates :http
  [action request]
  (action (-> request :params :id model.conversation/fetch-by-id)))
