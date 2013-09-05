(ns jiksnu.modules.web.filters.feed-source-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.core.filters :refer [parse-page parse-sorting]]))

;; index

(deffilter #'actions.feed-source/index :http
  [action request]
  (action {} (merge {}
                    (parse-page request)
                    (parse-sorting request))))

;; process-updates

(deffilter #'actions.feed-source/process-updates :http
  [action request]
  (-> request :params action))

;; unsubscribe

(deffilter #'actions.feed-source/unsubscribe :http
  [action request]
  (if-let [source (-> request :params :id model.feed-source/fetch-by-id)]
    (action source)))

;; show

(deffilter #'actions.feed-source/show :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [user (model.feed-source/fetch-by-id id)]
     (action user))))

(deffilter #'actions.feed-source/update :http
  [action request]
  (if-let [source (-> request :params :id model.feed-source/fetch-by-id)]
    (action source {:force true})))

