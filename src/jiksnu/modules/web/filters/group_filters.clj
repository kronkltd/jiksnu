(ns jiksnu.modules.web.filters.group-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.user :as model.user]))

(deffilter #'actions.group/add :http
  [action request]
  (-> request :params action))

(deffilter #'actions.group/create :http
  [action request]
  (-> request :params action))

(deffilter #'actions.group/edit-page :http
  [action request]
  (action (model.group/fetch-by-name (:name (:params request)))))

;; index

(deffilter #'actions.group/index :http
  [action request]
  (action))

(deffilter #'actions.group/index :page
  [action request]
  (action))

;; show

(deffilter #'actions.group/show :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [item (model.group/fetch-by-id id)]
      (action item))))

(deffilter #'actions.group/new-page :http
  [action request]
  (action))

(deffilter #'actions.group/user-list :http
  [action request]
  (-> request :params :id
      model.user/fetch-by-id action))
