(ns jiksnu.http.controller.domain-controller
  (:use jiksnu.http.controller.webfinger-controller)
  (:require [jiksnu.model.domain :as model.domain]))

(defn create
  [request]
  (let [{{domain "domain"} :params} request]
    (model.domain/create {:_id domain})))

(defn index
  [request]
  (let [domains (model.domain/index)]
    domains))

(defn show
  [request]
  (let [{{id "*"} :params} request
        domain (model.domain/show id)]
    domain))

(defn check-webfinger
  [domain]
  true)

(defn discover
  [request]
  (let [{{id "id"} :params} request
        domain (model.domain/show id)
        xrd (fetch (str "http://" id "/.well-known/host-meta"))
        links (get-links xrd)]
    (model.domain/update (assoc domain :links links))))

(defn delete
  [request]
  (let [{{id "*"} :params} request
        response (model.domain/delete id)]
    response))
