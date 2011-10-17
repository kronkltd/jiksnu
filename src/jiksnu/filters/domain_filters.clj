(ns jiksnu.filters.domain-filters
  (:use (ciste [debug :only (spy)]
               [filters :only (deffilter)])
        jiksnu.actions.domain-actions)
  (:require (jiksnu.model [domain :as model.domain])))

(deffilter #'check-webfinger :http
  [action request]
  (action))

(deffilter #'create :http
  [action {{:keys [domain]} :params}]
  (action {:_id domain}))

(deffilter #'delete :http
  [action request]
  (-> request :params :* action))

(deffilter #'discover :http
  [action request]
  (-> request :params :* model.domain/show action))

(deffilter #'find-or-create :http
  [action request]
  (-> request :params :domain action))

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'show :http
  [action request]
  (-> request :params :* action))

(deffilter #'ping-error :xmpp
  [action request]
  (-> request :from .getDomain
      model.domain/show action))

(deffilter #'ping-response :xmpp
  [action request]
  (-> request :from .getDomain
      model.domain/show action))

(deffilter #'host-meta :http
  [action request]
  (action))

