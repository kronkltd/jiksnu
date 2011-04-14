(ns jiksnu.actions.domain-actions
  (:use ciste.core
        ciste.debug
        jiksnu.actions.webfinger-actions)
  (:require [jiksnu.model.domain :as model.domain]))

(defaction check-webfinger
  [domain]
  true)

(defaction create
  [request]
  (let [{{domain "domain"} :params} request]
    (model.domain/create {:_id domain})))

(defaction delete
  [request]
  (let [{{id "*"} :params} request
        response (model.domain/delete id)]
    response))

(defaction discover
  [id]
  (model.domain/show id))

(defaction edit
  [id]
  (model.domain/show id))

(defaction index
  [request]
  (let [domains (model.domain/index)]
    domains))

(defaction show
  [request]
  (let [{{id "*"} :params} request
        domain (model.domain/show id)]
    domain))

(defaction find-or-create
  [id]
  (if-let [domain (model.domain/show id)]
    domain
    (create id)))

(defaction ping
  [domain]
  true)
