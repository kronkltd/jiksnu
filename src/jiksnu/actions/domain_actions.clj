(ns jiksnu.actions.domain-actions
  (:use (ciste [config :only [config definitializer]]
               [core :only [defaction]]
               [debug :only [spy]])
        (clojure.core [incubator :only [-?>>]]))
  (:require (ciste [model :as cm])
            (clj-tigase [core :as tigase])
            (clojure [string :as string])
            (clojure.data [json :as json])
            (clojure.tools [logging :as log])
            (jiksnu.model [domain :as model.domain]
                          [webfinger :as model.webfinger])
            (karras [sugar :as sugar])
            (ring.util [codec :as codec]))
  (:import java.net.URL
           jiksnu.model.Domain))

(defaction create
  [options]
  (let [prepared-domain (assoc options :discovered false)]
    (model.domain/create prepared-domain)))

(defaction delete
  [id]
  (model.domain/delete id))

(defaction update
  [domain]
  (model.domain/update domain))

(defn discover-onesocialweb
  [domain]
  (-> domain
      model.domain/ping-request
      tigase/make-packet
      tigase/deliver-packet!)
  domain)

(defn discover-webfinger
  [^Domain domain]
  ;; TODO: check https first
  (if-let [xrd (-> domain
                   model.domain/host-meta-link
                   model.webfinger/fetch-host-meta)]
    (if-let [links (model.webfinger/get-links xrd)]
      (update (-> domain
                  (assoc :links links)
                  (assoc :discovered true)))
      (throw (RuntimeException. "Host meta does not have any links")))
    (throw (RuntimeException.
            (str "Could not find host meta for domain: " (:_id domain))))))

(defaction edit-page
  [id]
  (model.domain/fetch-by-id id))

(defaction index
  []
  (model.domain/fetch-all
   {} :sort [(sugar/asc :_id)]
   :limit 20

   ))

(defaction show
  [domain]
  domain)

(defn find-or-create
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

(defaction set-discovered!
  "marks the domain as having been discovered"
  [domain]
  (model.domain/set-field domain :discovered true))

(defaction set-xmpp
  [domain value]
  (model.domain/set-field domain :xmpp false))

(defaction ping-response
  [domain]
  (-> domain
      (assoc :xmpp true)
      set-discovered!
      model.domain/update))

(defn fetch-statusnet-config
  ([domain] (fetch-statusnet-config domain ""))
  ([domain context]
     (when-let [doc (cm/fetch-resource (str "http://" (:_id domain) context "/api/statusnet/config.json"))]
       (json/read-json doc))))

(defaction discover
  [domain]
  (-> domain
      discover-onesocialweb
      discover-webfinger
      (assoc :statusnet-config (fetch-statusnet-config domain))
      update))

(defn get-user-meta-url
  [domain user-uri]
  (-?>> domain
        :links
        (filter #(= (:rel %) "lrdd"))
        (map #(string/replace (:template %) #"\{uri\}" (codec/url-encode user-uri)))
        first))


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
