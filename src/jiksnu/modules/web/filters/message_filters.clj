(ns jiksnu.modules.web.filters.message-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.message-actions :as actions.message]
            [jiksnu.model.user :as model.user]))

(deffilter #'actions.message/inbox-page :http
  [action request]
  (-> request :params :username model.user/get-user action))

(deffilter #'actions.message/outbox-page :http
  [action request]
  (-> request :params :username model.user/get-user action))
