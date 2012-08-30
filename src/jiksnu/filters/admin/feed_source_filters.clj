(ns jiksnu.filters.admin.feed-source-filters
  (:use [ciste.filters :only [deffilter]]
        [clojure.core.incubator :only [-?>]]
        jiksnu.actions.admin.feed-source-actions
        [jiksnu.filters :only [parse-page parse-sorting]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]))

(deffilter #'add-watcher :http
  [action request]
  (if-let [source (-?> request :params :id model/make-id model.feed-source/fetch-by-id)]
    (if-let [watcher (-?> request :params :user_id model.user/get-user)]
      (action source watcher))))

(deffilter #'delete :command
  [action id]
  (let [item (model.feed-source/fetch-by-id (model/make-id id))]
    (action item)))

(deffilter #'delete :http
  [action request]
  (if-let [source (-?> request :params :id model/make-id model.feed-source/fetch-by-id)]
    (action source)))

(deffilter #'fetch-updates :http
  [action request]
  (if-let [source (-?> request :params :id model/make-id model.feed-source/fetch-by-id)]
    (action source)))

(deffilter #'index :http
  [action request]
  (action
   {} (merge
       {}
       (parse-page request)
       (parse-sorting request))))

(deffilter #'remove-watcher :http
  [action request]
  (if-let [source (-?> request :params :id model/make-id model.feed-source/fetch-by-id)]
    (if-let [watcher (-?> request :params :user_id model/make-id model.user/fetch-by-id)]
      (action source watcher))))

(deffilter #'show :http
  [action request]
  (if-let [source (-?> request :params :id model/make-id model.feed-source/fetch-by-id)]
    (action source)))
