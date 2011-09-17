(ns jiksnu.actions.domain-actions
  (:use (ciste [core :only (defaction)]
               [debug :only (spy)]))
  (:require (clj-tigase [core :as tigase])
            (jiksnu.model [domain :as model.domain])))

(defaction check-webfinger
  [domain]
  true)

(defaction create
  [options]
  (let [prepared-domain (assoc options :discovered false)]
    (model.domain/create options)))

(defaction delete
  [id]
  (model.domain/delete id))

(defaction discover
  [id]
  (model.domain/show id))

(defaction edit
  [id]
  (model.domain/show id))

(defaction index
  []
  (model.domain/index))

(defaction show
  [id]
  (model.domain/show id))

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
  (model.domain/set-field domain :xmpp false)
  false)

(defaction ping-response
  [domain]
  (-> domain
      (assoc :xmpp true)
      (assoc :discovered true)
      model.domain/update))

(defaction set-xmpp
  [domain value]
  (model.domain/set-field domain :xmpp false))

(defn get-user-meta-uri
  [domain username]
  ;; TODO: find the template and insert the username
  "")

(defn discover-onesocialweb
  [domain]
  (-> domain
      model.domain/ping-request
      tigase/make-packet
      tigase/deliver-packet!))
