(ns jiksnu.filters.feed-source-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.feed-source-actions :only [process-updates
                                                   update
                                                   delete
                                                   index
                                                   show
                                                   subscribe
                                                   remove-subscription
                                                   watch]]
        [jiksnu.filters :only [parse-page parse-sorting]])
  (:require [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]))

;; delete

(deffilter #'delete :command
  [action id]
  (let [item (model.feed-source/fetch-by-id (model/make-id id))]
    (action item)))

;; index

(deffilter #'index :http
  [action request]
  (action {} (merge {}
                    (parse-page request)
                    (parse-sorting request))))

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

;; subscribe

(deffilter #'subscribe :command
  [action id]
  (let [item (model.feed-source/fetch-by-id (model/make-id id))]
    (action item)))

;; update

(deffilter #'update :command
  [action id]
  (let [item (model.feed-source/fetch-by-id (model/make-id id))]
    (action item)))

(deffilter #'update :http
  [action request]
  (if-let [source (-> request :params :id model/make-id model.feed-source/fetch-by-id)]
    (action source)))

;; watch

(deffilter #'watch :command
  [action id]
  (action (model.feed-source/fetch-by-id (model/make-id id))))
