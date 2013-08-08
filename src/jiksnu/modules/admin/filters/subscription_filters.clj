(ns jiksnu.modules.admin.filters.subscription-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.modules.admin.actions.subscription-actions)
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.util :as util]))

(deffilter #'create :http
  [action request]
  ;; TODO: injection
  (-> request :params action))

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'show :http
  [action request]
  (action (-> request :params :id util/make-id model.subscription/fetch-by-id)))

(deffilter #'delete :http
  [action request]
  (action (-> request :params :id util/make-id model.subscription/fetch-by-id)))

(deffilter #'update :http
  [action request]
  (action (-> request :params :id util/make-id model.subscription/fetch-by-id)))
