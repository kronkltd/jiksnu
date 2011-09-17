(ns jiksnu.filters.domain-filters
  (:use (ciste [debug :only (spy)]
               [filters :only (deffilter)])
        jiksnu.actions.domain-actions)
  (:require (jiksnu.model [domain :as model.domain])))

(deffilter #'check-webfinger :http
  [action domain]
  true)

;; TODO: creating the domain should be performed in the action
(deffilter #'create :http
  [action request]
  (let [{{domain :domain} :params} request]
    (model.domain/create {:_id domain})))

;; TODO: deleting the domain should be done in the action
(deffilter #'delete :http
  [action request]
  (let [{{id :*} :params} request
        response (model.domain/delete id)]
    response))

(deffilter #'discover :http
  [action request]
  (let [{{id :*} :params} request
        domain (model.domain/show id)]
    (action domain)))

(deffilter #'index :http
  [action request]
  (let [domains (model.domain/index)]
    domains))

(deffilter #'show :http
  [action request]
  (let [{{id :*} :params} request
        domain (model.domain/show id)]
    domain))

(deffilter #'ping-response :xmpp
  [action request]
  (let [id (.getDomain (:from request))
        domain (model.domain/show id)]
    (action domain)))
