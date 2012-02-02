(ns jiksnu.filters.message-filters
  (:use (ciste [filters :only [deffilter]])
        jiksnu.actions.message-actions)
  (:require (jiksnu.model [user :as model.user])))

(deffilter #'inbox-page :http
  [action request]
  (-> request :params :username model.user/show action))

(deffilter #'outbox-page :http
  [action request]
  (-> request :params :username model.user/show action))
