(ns jiksnu.modules.admin.filters.feed-source-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source] 
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.admin.actions.feed-source-actions :refer :all]
            [jiksnu.modules.core.filters :refer [parse-page parse-sorting]]
            [jiksnu.util :as util]))

;; add-watcher

(deffilter #'add-watcher :http
  [action request]
  (if-let [source (some-> request :params :id model.feed-source/fetch-by-id)]
    (if-let [watcher (some-> request :params :user_id model.user/get-user)]
      (action source watcher))))

;; delete

(deffilter #'delete :command
  [action id]
  (let [item (model.feed-source/fetch-by-id id)]
    (action item)))

(deffilter #'delete :http
  [action request]
  (if-let [source (some-> request :params :id model.feed-source/fetch-by-id)]
    (action source)))

;; fetch-updates

(deffilter #'fetch-updates :http
  [action request]
  (if-let [source (some-> request :params :id model.feed-source/fetch-by-id)]
    (action source)))

;; index

(deffilter #'index :http
  [action request]
  (action
   {} (merge
       {}
       (parse-page request)
       (parse-sorting request))))

;; remove-watcher

(deffilter #'remove-watcher :http
  [action request]
  (let [params (:params request)]
    (if-let [source (some-> params :id model.feed-source/fetch-by-id)]
      (if-let [watcher (some-> params :user_id model.user/fetch-by-id)]
        (action source watcher)))))

;; show

(deffilter #'show :http
  [action request]
  (if-let [source (some-> request :params :id model.feed-source/fetch-by-id)]
    (action source)))
