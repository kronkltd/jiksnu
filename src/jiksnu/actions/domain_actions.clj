(ns jiksnu.actions.domain-actions
  (:use [ciste.config :only [config definitializer]]
        [ciste.core :only [defaction]]
        [ciste.runner :only [require-namespaces]]
        [clojure.core.incubator :only [-?>>]])
  (:require [ciste.model :as cm]
            [clj-tigase.core :as tigase]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.webfinger :as model.webfinger]
            [ring.util.codec :as codec])
  (:import java.net.URL
           jiksnu.model.Domain))

(defaction create
  [options]
  (let [prepared-domain (assoc options :discovered false)]
    (model.domain/create prepared-domain)))

(defaction delete
  [domain]
  (model.domain/delete domain))

(defaction update
  [domain]
  (model.domain/update domain))

(defn discover-onesocialweb
  [domain url]
  (-> domain
      model.domain/ping-request
      tigase/make-packet
      tigase/deliver-packet!)
  domain)

(defn path-segments
  [url]
  (if url
    (let [url-obj (URL. url)
          path (.getPath url-obj)
          ps (string/split path #"/")
          bare (str "http://" (.getHost url-obj))]
      (map #(str bare % "/")
           (reductions (fn [s1 s2] (clojure.string/join "/" [s1 s2]))
                       (drop-last ps))))
    []))

(defn discover-webfinger
  [^Domain domain url]
  ;; TODO: check https first
  (if-let [xrd (first (keep
                       (fn [url]
                         (try
                           (model.webfinger/fetch-host-meta url)
                           (catch RuntimeException ex
                             (log/error "Fetching host meta failed"))))
                       (cons
                        (model.domain/host-meta-link domain)
                        (map
                         #(str % ".well-known/host-meta")
                         (rest (path-segments url))))))]
    (if-let [links (model.webfinger/get-links xrd)]
      ;; TODO: do individual updates
      (update (-> domain
                  (assoc :links links)
                  (assoc :discovered true)))
      (throw (RuntimeException. "Host meta does not have any links")))
    (throw (RuntimeException.
            (str "Could not find host meta for domain: " (:_id domain))))))

(defaction show
  [domain]
  domain)

(defaction edit-page
  [domain]
  domain)

(defaction index
  [& [options & _]]
  (let [page (Integer/parseInt (get options :page "1"))
        page-size 20
        criteria {:sort [{:_id 1}]
                  :skip (* (dec page) page-size)
                  :limit page-size}
        total-records (model.domain/count-records {})
        records (model.domain/fetch-all (:where options) criteria)]
    {:items records
     :page page
     :page-size page-size
     :total-records total-records
     :args options}))

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
  ([domain] (fetch-statusnet-config domain nil))
  ([domain context]
     (when-let [doc (cm/fetch-resource (str "http://" (:_id domain) context "/api/statusnet/config.json"))]
       (json/read-json doc))))

(defn discover-statusnet-config
  [domain url]
  (-> domain
      (assoc :statusnet-config (fetch-statusnet-config domain))
      update))

(defaction discover
  [^Domain domain url]
  (log/debugf "discovering domain - %s" (:_id domain))
  (future (discover-webfinger domain url))
  (future (discover-onesocialweb domain url))
  (future (discover-statusnet-config domain url))
  (model.domain/fetch-by-id (:_id domain)))

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
  (require-namespaces
   ["jiksnu.filters.domain-filters"
    "jiksnu.triggers.domain-triggers"
    "jiksnu.views.domain-views"]))
