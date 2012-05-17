(ns jiksnu.filters.admin.feed-source-filters
  (:use [ciste.debug :only [spy]]
        [ciste.filters :only [deffilter]]
        jiksnu.actions.admin.feed-source-actions)
  (:require [jiksnu.model.feed-source :as model.feed-source]))

(deffilter #'index :http
  [action request]
  (spy (action)))

(deffilter #'show :http
  [action request]
  (-> request :params :id model.feed-source/fetch-by-id action))
