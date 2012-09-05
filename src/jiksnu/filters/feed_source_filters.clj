(ns jiksnu.filters.feed-source-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.feed-source-actions :only [process-updates
                                                   update
                                                   delete
                                                   show
                                                   remove-subscription]])
  (:require [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]))

;; delete

(deffilter #'delete :command
  [action id]
  (let [item (model.feed-source/fetch-by-id (model/make-id id))]
    (action item)))

;; process-updates

(deffilter #'process-updates :http
  [action request]
  (-> request :params action))

;; remove-subscription

(deffilter #'remove-subscription :http
  [action request]
  (if-let [source (-> request :params :id model/make-id model.feed-source/fetch-by-id)]
    (action source)))

;; show

(deffilter #'show :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [user (model.feed-source/fetch-by-id (model/make-id id))]
     (action user))))

;; update

(deffilter #'update :command
  [action id]
  (let [item (model.feed-source/fetch-by-id (model/make-id id))]
    (action item)))

(deffilter #'update :http
  [action request]
  (if-let [source (-> request :params :id model/make-id model.feed-source/fetch-by-id)]
    (action source)))
