(ns jiksnu.filters.feed-source-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.feed-source-actions :only [process-updates
                                                   fetch-updates
                                                   remove-subscription]])
  (:require [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]))

(deffilter #'process-updates :http
  [action request]
  (-> request :params action))

(deffilter #'remove-subscription :http
  [action request]
  (if-let [source (-> request :params :id model/make-id model.feed-source/fetch-by-id)]
    (action source)))

(deffilter #'fetch-updates :http
  [action request]
  (if-let [source (-> request :params :id model/make-id model.feed-source/fetch-by-id)]
    (action source)))
