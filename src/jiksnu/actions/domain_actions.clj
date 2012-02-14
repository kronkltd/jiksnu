(ns jiksnu.actions.domain-actions
  (:use (ciste [config :only [config definitializer]]
               [core :only [defaction]]
               [debug :only [spy]])
        (clojure.core [incubator :only [-?>>]]))
  (:require (clj-tigase [core :as tigase])
            (clojure [string :as string])
            (clojure.tools [logging :as log])
            (jiksnu.model [domain :as model.domain]
                          [webfinger :as model.webfinger])
            (ring.util [codec :as codec]))
  (:import java.net.URL
           jiksnu.model.Domain))

(defaction check-webfinger
  [domain]
  true)

(defaction create
  [options]
  (let [prepared-domain (assoc options :discovered false)]
    (model.domain/create prepared-domain)))

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
    (if-let [links (model.webfinger/get-links xrd)]
      ;; TODO: These should call actions
      (do (model.domain/add-links domain links)
          (model.domain/set-discovered domain)
          domain)
      (throw (RuntimeException. "Host meta does not have any links")))
    (throw (RuntimeException.
            (str "Could not find host meta for domain: " (:_id domain))))))

(defaction edit-page
  [id]
  (model.domain/fetch-by-id id))

(defaction index
  []
  (model.domain/index))

(defaction show
  [domain]
  (model.domain/fetch-by-id (:_id domain)))

(defaction find-or-create
  [id]
  (or (model.domain/fetch-by-id id)
      (create {:_id id})))

(defn find-or-create-for-url
  "Return a domain object that matche the domain of the provided url"
  [url]
  (let [url-obj (URL. url)]
    (find-or-create (.getHost url-obj))))

(defn current-domain
  []
  (find-or-create (config :domain)))

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

(defaction set-discovered!
  "marks the domain as having been discovered"
  [domain]
  (model.domain/set-field domain :discovered true))

(defaction discover
  [domain]
  (future (discover-onesocialweb domain))
  (future (discover-webfinger domain))
  (set-discovered! domain))

(defn get-user-meta-url
  [domain user-uri]
  (-?>> domain
       :links
       (filter #(= (:rel %) "lrdd"))
       (map #(string/replace (:template %) #"\{uri\}" (codec/url-encode user-uri)))
       first))

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

(definitializer
  (doseq [namespace ['jiksnu.filters.domain-filters
                     'jiksnu.triggers.domain-triggers
                     'jiksnu.views.domain-views]]
    (require namespace)))
