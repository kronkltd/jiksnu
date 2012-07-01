(ns jiksnu.filters.admin.subscription-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.admin.subscription-actions)
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]))

(deffilter #'create :http
  [action request]
  ;; TODO: injection
  (-> request :params action))

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'show :http
  [action request]
  (action (-> request :params :id model/make-id model.subscription/fetch-by-id)))

(deffilter #'delete :http
  [action request]
  (action (-> request :params :id model/make-id model.subscription/fetch-by-id)))

(deffilter #'update :http
  [action request]
  (action (-> request :params :id model/make-id model.subscription/fetch-by-id)))
