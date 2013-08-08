(ns jiksnu.modules.core.filters.message-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.message-actions)
  (:require [jiksnu.model.user :as model.user]))

(deffilter #'inbox-page :http
  [action request]
  (-> request :params :username model.user/get-user action))

(deffilter #'outbox-page :http
  [action request]
  (-> request :params :username model.user/get-user action))
