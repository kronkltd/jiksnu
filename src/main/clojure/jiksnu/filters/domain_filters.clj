(ns jiksnu.filters.domain-filters
  (:use ciste.debug
        ciste.filters
        jiksnu.actions.domain-actions
        jiksnu.actions.webfinger-actions)
  (:require [jiksnu.model.domain :as model.domain]))

(deffilter #'check-webfinger :http
  [action domain]
  true)

(deffilter #'create :http
  [action request]
  (let [{{domain :domain} :params} request]
    (model.domain/create {:_id domain})))

(deffilter #'delete :http
  [action request]
  (let [{{id :*} :params} request
        response (model.domain/delete id)]
    response))

(deffilter #'discover :http
  [action request]
  (let [{{id :*} :params} request
        domain (model.domain/show id)
        xrd (fetch (str "http://" id "/.well-known/host-meta"))
        links (get-links xrd)
        new-domain (assoc domain :links links)]
    (model.domain/update new-domain)))

(deffilter #'index :http
  [action request]
  (let [domains (model.domain/index)]
    domains))

(deffilter #'show :http
  [action request]
  (let [{{id :*} :params} request
        domain (model.domain/show id)]
    domain))
