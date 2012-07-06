(ns jiksnu.filters.admin.feed-source-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.admin.feed-source-actions)
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]))

(deffilter #'index :http
  [action request]
  (let [order-by (:orderBy (:params request))
        direction (if (= "desc" (:direction (:params request))) -1 1)]
    (action
     {} (merge
         (when (and order-by direction)
           {:sort-clause {(keyword order-by) direction}})))))

(deffilter #'show :http
  [action request]
  (if-let [source (-> request :params :id model/make-id model.feed-source/fetch-by-id)]
    (action source)))

(deffilter #'delete :http
  [action request]
  (if-let [source (-> request :params :id model/make-id model.feed-source/fetch-by-id)]
    (action source)))
