(ns jiksnu.filters.domain-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.domain-actions)
  (:require [jiksnu.model.domain :as model.domain])
  (:import tigase.xml.Element))

(deffilter #'create :http
  [action {{:keys [domain]} :params}]
  (action {:_id domain}))

(deffilter #'delete :http
  [action request]
  (-> request :params :id action))

(deffilter #'discover :http
  [action request]
  (-> request :params :id model.domain/fetch-by-id action))

(deffilter #'find-or-create :http
  [action request]
  (-> request :params :domain action))

(deffilter #'index :http
  [action request]
  (action (:params request)))

(deffilter #'show :http
  [action request]
  (-> request :params :id model.domain/fetch-by-id action))

(deffilter #'ping-error :xmpp
  [action request]
  (-> request :from .getDomain
      model.domain/fetch-by-id action))

(deffilter #'ping-response :xmpp
  [action request]
  (-> request :from .getDomain
      model.domain/fetch-by-id action))

(deffilter #'host-meta :http
  [action request]
  (action))

