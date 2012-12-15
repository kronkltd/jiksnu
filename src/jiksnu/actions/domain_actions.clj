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
            [jiksnu.actions :as actions]
            [jiksnu.channels :as ch]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.ops :as ops]
            [jiksnu.templates :as templates]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.domain-transforms :as transforms.domain]
            [jiksnu.util :as util]
            [lamina.core :as l]
            [lamina.executor :only [task]]
            [lamina.time :as time]
            [monger.collection :as mc]
            [ring.util.codec :as codec])
  (:import java.net.URL
           jiksnu.model.Domain))

(defonce delete-hooks (ref []))
(defonce pending-discovers (ref {}))

(defn prepare-create
  [domain]
  (-> domain
      transforms.domain/set-local
      transforms.domain/set-discovered
      transforms/set-created-time
      transforms/set-updated-time))

(defn prepare-delete
  ([domain]
     (prepare-delete domain @delete-hooks))
  ([domain hooks]
     (if (seq hooks)
       (recur ((first hooks) domain) (rest hooks))
       domain)))

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
  (let [resource (ops/get-resource url)
        response (ops/update-resource resource)]
    (try
      (if-let [body (:body response)]
        (cm/string->document body)
        (throw+ "Document did not contain any data"))
      (catch RuntimeException ex
        (log/error "Fetching host meta failed")
        (.printStackTrace ex)))))

(defn fetch-xrd
  [domain url]
  (if-let [xrd (->> url util/path-segments rest
                    (map #(str % ".well-known/host-meta"))
                    (cons (model.domain/host-meta-link domain))
                    (keep fetch-xrd*) first)]
    xrd
    (throw+
     {:message "could not determine host meta"
      :domain domain
      :url url})))

(defaction set-discovered!
  "marks the domain as having been discovered"
  [domain]
  (model.domain/set-field! domain :discovered true)
  (let [id (:_id domain)
        domain (model.domain/fetch-by-id id)]
    (when-let [p (get @pending-discovers id)]
      (let [domain (model.domain/fetch-by-id (:_id domain))]
        (deliver p domain)))
    domain))

(defn discover-webfinger
  [^Domain domain url]
  ;; TODO: check https first
  (if-let [xrd (fetch-xrd domain url) ]
    (if-let [links (model.webfinger/get-links xrd)]
      ;; TODO: do individual updates
      (do
        (doseq [link links]
          (add-link domain link))
        (set-discovered! domain)
        domain)
      (throw+  "Host meta does not have any links"))
    (throw+ (format "Could not find host meta for domain: %s" (:_id domain)))))

(defaction edit-page
  [domain]
  domain)

(defaction show
  [domain]
  domain)

(def index*
  (templates/make-indexer 'jiksnu.model.domain
                      :sort-clause {:username 1}))

(defaction index
  [& options]
  (apply index* options))

(defaction ping
  [domain]
  true)

;; Occurs if the ping request caused an error
(defaction ping-error
  [domain]
  (model.domain/set-field! domain :xmpp false)
  false)

(defaction set-xmpp
  [domain value]
  (model.domain/set-field! domain :xmpp false))

(defaction ping-response
  [domain]
  (set-xmpp domain true))

(defn statusnet-url
  [domain]
  (str "http://" (:_id domain) (:context domain) "/api/statusnet/config.json"))

(defn discover-statusnet-config
  [domain url]
  (let [resource (ops/get-resource (statusnet-url domain))
        response (actions/invoke-action "resource" "update*" (str (:_id resource)))
        sconfig (json/read-json (:body (:body response)))]
    (model.domain/set-field! domain :statusnet-config sconfig)))

(defn discover*
  [domain url]
  (task (discover-webfinger domain url))
  (task (discover-onesocialweb domain url))
  (task (discover-statusnet-config domain url)))

(defaction discover
  [^Domain domain url]
  (if-not (:local domain)
    (do (log/debugf "discovering domain - %s" (:_id domain))
        (discover* domain url)
        (model.domain/fetch-by-id (:_id domain)))
    (log/warn "local domains do not need to be discovered")))

(defaction create
  [params]
  (let [item (prepare-create params)]
    (model.domain/create item)))

(defn find-or-create
  [params]
  (or (model.domain/fetch-by-id (:_id params))
      (create params)))

(defn find-or-create-for-url
  "Return a domain object that matche the domain of the provided url"
  [url]
  (let [url-obj (URL. url)]
    (find-or-create (.getHost url-obj))))

(defn current-domain
  []
  (find-or-create {:_id (config :domain)
                   :local true}))

(defn get-discovered
  [domain]
  (let [domain (find-or-create domain)]
    (if (:discovered domain)
      domain
      (let [id (:_id domain)
            p (dosync
               (when-not (get @pending-discovers id)
                 (let [p (promise)]
                   (alter pending-discovers #(assoc % id p))
                   p)))
            p (if p
                (do (discover domain) p)
                (get @pending-discovers id))]
        (or (deref p (time/seconds 30) nil)
            (throw+ "Could not discover domain"))))))

(defn get-user-meta-url
  [domain user-uri]
  (when user-uri
    (-?>> domain
          :links
          (filter #(= (:rel %) "lrdd"))
          (map #(string/replace (:template %) #"\{uri\}" (codec/url-encode user-uri)))
          first)))

(defaction host-meta
  []
  (let [domain (config :domain)
        template (str "http://" domain "/main/xrd?uri={uri}")]
    {:host domain
     :links [{:template template
              :rel "lrdd"
              :title "Resource Descriptor"}]}))

(defn- handle-pending-get-domain
  [[p domain-name]]
  (deliver p (find-or-create {:_id domain-name})))

(l/receive-all ch/pending-get-domain handle-pending-get-domain)

(definitializer
  (current-domain)
  (require-namespaces
   ["jiksnu.filters.domain-filters"
    "jiksnu.triggers.domain-triggers"
    "jiksnu.views.domain-views"]))
