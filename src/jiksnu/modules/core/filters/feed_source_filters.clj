(ns jiksnu.modules.core.filters.feed-source-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.feed-source-actions :only [process-updates
                                                   update
                                                   delete
                                                   index
                                                   show
                                                   subscribe
                                                   unsubscribe
                                                   watch]]
        [jiksnu.modules.core.filters :only [parse-page parse-sorting]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]
            [jiksnu.util :as util]))

;; delete

(deffilter #'delete :command
  [action id]
  (if-let [item (model.feed-source/fetch-by-id id)]
    (action item)))

;; index

(deffilter #'index :http
  [action request]
  (action {} (merge {}
                    (parse-page request)
                    (parse-sorting request))))

(deffilter #'index :page
  [action request]
  (action))

;; process-updates

(deffilter #'process-updates :http
  [action request]
  (-> request :params action))

;; unsubscribe

(deffilter #'unsubscribe :command
  [action id]
  (if-let [item (model.feed-source/fetch-by-id id)]
    (action item)))

(deffilter #'unsubscribe :http
  [action request]
  (if-let [source (-> request :params :id model.feed-source/fetch-by-id)]
    (action source)))

;; show

(deffilter #'show :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [user (model.feed-source/fetch-by-id id)]
     (action user))))

;; subscribe

(deffilter #'subscribe :command
  [action id]
  (let [item (model.feed-source/fetch-by-id id)]
    (action item)))

;; update

(deffilter #'update :command
  [action id]
  (let [item (model.feed-source/fetch-by-id id)]
    (action item {:force true})))

(deffilter #'update :http
  [action request]
  (if-let [source (-> request :params :id model.feed-source/fetch-by-id)]
    (action source {:force true})))

;; watch

(deffilter #'watch :command
  [action id]
  (action (model.feed-source/fetch-by-id id)))
