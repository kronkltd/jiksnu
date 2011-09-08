(ns jiksnu.actions.domain-actions
  (:use (ciste [core :only (defaction)]
               [debug :only (spy)]))
  (:require (jiksnu.model [domain :as model.domain])))

(defaction check-webfinger
  [domain]
  true)

(defaction create
  [options]
  (let [prepared-domain (assoc options :discovered false)]
    (model.domain/create options)))

(defaction delete
  [request]
  (let [{{id "*"} :params} request
        response (model.domain/delete id)]
    response))

(defaction discover
  [domain]
  domain)

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
  (or (model.domain/show id)
      (create {:_id id})))

(defaction ping
  [domain]
  true)

;; Occurs if the ping request caused an error
(defaction ping-error
  [domain]
  (model.domain/update (assoc-in domain [:enabled :xmpp] false))
  false)

(defaction ping-response
  [domain]
  (model.domain/update
   (merge domain {:xmpp true
                  :discovered true})))

(defaction set-xmpp
  [domain value]
  (model.domain/set-field domain :xmpp false))

(defn get-user-meta-uri
  [domain username]
  ;; TODO: find the template and insert the username
  "")
