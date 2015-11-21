(ns jiksnu.modules.web.filters.comment-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.comment-actions :as actions.comment]
            [jiksnu.model.activity :as model.activity]))

(deffilter #'actions.comment/add-comment :http
  [action request]
  (-> request :params action))

(deffilter #'actions.comment/fetch-comments :http
  [action request]
  (-> request :params :id model.activity/fetch-by-id action))

;; (deffilter #'actions.comment/new-comment :http
;;   [action request]
;;   (-> request :params :id model.activity/fetch-by-id))
