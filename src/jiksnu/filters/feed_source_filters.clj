(ns jiksnu.filters.feed-source-filters
  (:use (ciste [debug :only [spy]]
               [filters :only [deffilter]])
        (jiksnu.actions [feed-source-actions :only [process-updates
                                                    fetch-updates
                                                    remove-subscription]]))
  (:require (jiksnu.model [feed-source :as model.feed-source]
                          [user :as model.user])))

(deffilter #'process-updates :http
  [action request]
  (-> request :params action))

(deffilter #'remove-subscription :http
  [action request]
  (-> request :params :id model.feed-source/fetch-by-id action))

(deffilter #'fetch-updates :http
  [action request]
  (-> request :params :id model.feed-source/fetch-by-id action))
