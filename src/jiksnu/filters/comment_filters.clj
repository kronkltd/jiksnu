(ns jiksnu.filters.comment-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.comment-actions)
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.activity :as model.activity]))

(deffilter #'add-comment :http
  [action request]
  (-> request :params action))

(deffilter #'fetch-comments :http
  [action request]
  (-> request :params :id model.activity/fetch-by-id action))

(deffilter #'new-comment :http
  [action request]
  (-> request :params :id model.activity/fetch-by-id))

