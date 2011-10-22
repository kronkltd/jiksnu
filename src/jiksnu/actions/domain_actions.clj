(ns jiksnu.actions.domain-actions
  (:use (ciste [config :only [config definitializer]]
               [core :only [defaction]]
               [debug :only [spy]])
        (jiksnu [model :only [with-database]]))
  (:require (clj-tigase [core :as tigase])
            (clojure.tools [logging :as log])
            (jiksnu.model [domain :as model.domain]
                          [webfinger :as model.webfinger]))
  (:import jiksnu.model.Domain))

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

(defn discover-onesocialweb
  [domain]
  (-> domain
      model.domain/ping-request
      tigase/make-packet
      tigase/deliver-packet!))

(defn discover-webfinger
  [^Domain domain]
  ;; TODO: check https first
  (if-let [xrd (-> domain
                   model.domain/host-meta-link
                   model.webfinger/fetch-host-meta)]
    (do (if-let [links (model.webfinger/get-links xrd)]
          ;; TODO: These should call actions
          (do (model.domain/add-links domain links)
              (model.domain/set-discovered domain))
          (log/error "Host meta does not have any links"))
        xrd)
    (log/error
     (str "Could not find host meta for domain: " (:_id domain)))))

(defaction discover
  [domain]
  (discover-onesocialweb domain)
  (spy (discover-webfinger domain)))

(defaction edit-page
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

(defn current-domain
  []
  (find-or-create (config :domain)))

(definitializer
  (with-database
    (current-domain)))

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

(defaction update
  [domain]
  (model.domain/update domain))


(defaction host-meta
  []
  (let [domain (config :domain)
        template (str "http://" domain "/main/xrd?uri={uri}")]
    {:host domain
     :links [{:template template
              :rel "lrdd"
              :title "Resource Descriptor"}]}))

