(ns jiksnu.actions.domain-actions
  (:use [ciste.config :only [config]]
        [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?>>]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clj-tigase.core :as tigase]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.webfinger :as model.webfinger]
            [monger.collection :as mc]
            [ring.util.codec :as codec])
  (:import java.net.URL
           jiksnu.model.Domain))

(defaction create
  [options]
  (model.domain/create options))

(defonce delete-hooks (ref []))

(defn prepare-delete
  ([domain]
     (prepare-delete domain @delete-hooks))
  ([domain hooks]
     (if (seq hooks)
       (recur ((first hooks) domain) (rest hooks))
       domain)))

(defaction delete
  [domain]
  (let [domain (prepare-delete domain)]
    (model.domain/delete domain)))

(defn discover-onesocialweb
  [domain url]
  (-> domain
      model.domain/ping-request
      tigase/make-packet
      tigase/deliver-packet!)
  domain)

(defn fetch-xrd*
  [url]
  (try
    (model.webfinger/fetch-host-meta url)
    (catch RuntimeException ex
      (log/error "Fetching host meta failed"))))

(defn fetch-xrd
  [domain url]
  (->> url model/path-segments rest
       (map #(str % ".well-known/host-meta"))
       (cons (model.domain/host-meta-link domain))
       (keep fetch-xrd*) first))

(defaction set-discovered!
  "marks the domain as having been discovered"
  [domain]
  (model.domain/set-field domain :discovered true))

(defaction add-link*
  [item link]
  (mc/update "domains" {:_id (:_id item)}
             {:$addToSet {:links link}})
  item)

;; FIXME: this is always hitting the else branch
(defn add-link
  [item link]
  (if-let [existing-link (model.domain/get-link item
                                                (:rel link)
                                                (:type link))]
    item
    (add-link* item link)))


(defn discover-webfinger
  [^Domain domain url]
  ;; TODO: check https first
  (if-let [xrd (fetch-xrd domain url) ]
    (if-let [links (model.webfinger/get-links xrd)]
      ;; TODO: do individual updates
      (do
        (set-discovered! domain)
        (doseq [link links]
          (add-link link))
        domain)
      (throw+  "Host meta does not have any links"))
    (throw+ (format "Could not find host meta for domain: %s" (:_id domain)))))

(defaction edit-page
  [domain]
  domain)

(defaction show
  [domain]
  domain)

(defaction index
  [& [options & _]]
  ;; TODO: to the filter
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
  [domain]
  (or (model.domain/fetch-by-id (:_id domain))
      (create domain)))

(defn find-or-create-for-url
  "Return a domain object that matche the domain of the provided url"
  [url]
  (let [url-obj (URL. url)]
    (find-or-create (.getHost url-obj))))

(defn current-domain
  []
  (find-or-create {:_id (config :domain)
                   :local true}))

(defaction ping
  [domain]
  true)

;; Occurs if the ping request caused an error
(defaction ping-error
  [domain]
  (model.domain/set-field domain :xmpp false)
  false)

(defaction set-xmpp
  [domain value]
  (model.domain/set-field domain :xmpp false))

(defaction ping-response
  [domain]
  (set-xmpp domain true))

(defn fetch-statusnet-config
  ([domain] (fetch-statusnet-config domain nil))
  ([domain context]
     (when-let [doc (cm/fetch-resource (str "http://" (:_id domain) context "/api/statusnet/config.json"))]
       (json/read-json doc))))

(defn discover-statusnet-config
  [domain url]
  (let [sconfig (fetch-statusnet-config domain)]
    (model.domain/set-field domain :statusnet-config sconfig)))

(defaction discover
  [^Domain domain url]
  (when-not (:local domain)
    (log/debugf "discovering domain - %s" (:_id domain))
    (future (discover-webfinger domain url))
    (future (discover-onesocialweb domain url))
    (future (discover-statusnet-config domain url))
    (model.domain/fetch-by-id (:_id domain))))

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
  (current-domain)
  (require-namespaces
   ["jiksnu.filters.domain-filters"
    "jiksnu.triggers.domain-triggers"
    "jiksnu.views.domain-views"]))
