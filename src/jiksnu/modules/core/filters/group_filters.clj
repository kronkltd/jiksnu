(ns jiksnu.modules.core.filters.group-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.group-actions)
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.user :as model.user]
            [jiksnu.util :as util]))

(deffilter #'add :http
  [action request]
  (-> request :params action))

(deffilter #'create :http
  [action request]
  (-> request :params action))

(deffilter #'edit-page :http
  [action request]
  (action (model.group/fetch-by-name (:name (:params request)))))

;; index

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'index :page
  [action request]
  (action))

;; show

(deffilter #'show :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [item (model.group/fetch-by-id id)]
      (action item))))

(deffilter #'new-page :http
  [action request]
  (action))

(deffilter #'user-list :http
  [action request]
  (-> request :params :id
      model.user/fetch-by-id action))
