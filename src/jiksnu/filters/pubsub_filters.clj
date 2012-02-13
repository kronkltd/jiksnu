(ns jiksnu.filters.pubsub-filters
  (:use (ciste [debug :only [spy]]
               [filters :only [deffilter]])
        jiksnu.actions.pubsub-actions)
  (:require (jiksnu.model [user :as model.user])))

;; (deffilter #'callback :http
;;   [action request]
;;   (action request))

;; (deffilter #'admin-index :http
;;   [action request]
;;   (action))

;; TODO: extract hub params
(deffilter #'hub-dispatch :http
  [action request]
  (action (:params request)))

;; (deffilter #'subscribe :http
;;   [action request]
;;   (-> request :params :id
;;       model.user/fetch-by-id action))
