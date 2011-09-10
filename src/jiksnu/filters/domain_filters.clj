(ns jiksnu.filters.domain-filters
  (:use (ciste [debug :only (spy)]
               [filters :only (deffilter)])
        jiksnu.actions.domain-actions)
  (:require (jiksnu.model [domain :as model.domain])))

(deffilter #'check-webfinger :http
  [action request]
  (action))

(deffilter #'create :http
  [action request]
  (let [{:keys [params]} request]
    (action {:_id (:domain params)})))

(deffilter #'delete :http
  [action request]
  (-> request :params :* action))

(deffilter #'discover :http
  [action request]
  (-> request :params :* action))

(deffilter #'find-or-create :http
  [action request]
  (-> request :params :domain action))

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'show :http
  [action request]
  (-> request :params :* action))

(deffilter #'ping-response :xmpp
  [action request]
  (let [id (.getDomain (:from request))
        domain (model.domain/show id)]
    (action domain)))
