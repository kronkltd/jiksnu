(ns jiksnu.modules.core.actions.service-actions
  (:require [ciste.config :refer [config]]
            [ciste.model :as cm]
            [clj-time.core :as time]
            [clojure.data.json :as json]
            [jiksnu.modules.core.actions.domain-actions :as actions.domain]
            [jiksnu.modules.core.actions.resource-actions :as actions.resource]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.service :as model.service]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.ops :as ops]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.util :as util]
            [manifold.deferred :as d]
            [manifold.time :as lt]
            [slingshot.slingshot :refer [throw+ try+]]
            [taoensso.timbre :as timbre])
  (:import jiksnu.model.Domain
           (org.apache.http HttpStatus)))

(def model-ns 'jiksnu.model.service)

(defonce pending-discovers (ref {}))

(defonce delete-hooks (ref []))

(defn prepare-delete
  ([item]
   (prepare-delete item @delete-hooks))
  ([item hooks]
   (if (seq hooks)
     (recur ((first hooks) item) (rest hooks))
     item)))

(defn prepare-create
  [params]
  (-> params
      transforms/set-_id
      transforms/set-updated-time
      transforms/set-created-time
      transforms/set-no-links))

(defn create
  "Create a new service record"
  ([params]
   (create params nil))
  ([params options]
   (let [params (prepare-create params)]
     (model.service/create params))))

(def index*
  (templates.actions/make-indexer model-ns
                                  :sort-clause {:created -1}))

(defn index
  [& options]
  (apply index* options))

(defn delete
  [source]
  (model.service/delete source))

(defn fetch-xrd*
  [url]
  {:pre [(string? url)]}
  (try+
   (let [res (ops/update-resource url {:force true})]
     (d/on-realized res
                    (fn [_] (timbre/info "Finished fetching xrd"))
                    (fn [_] (timbre/error "Fetching xrd caused error")))

     (let [response @res]
       (when (= (:status response) HttpStatus/SC_OK)
         (try
           (if-let [body (:body response)]
             (cm/string->document body))
           (catch RuntimeException ex
             (timbre/error ex "Fetching host meta failed"))))))
   (catch Object ex
     (timbre/error ex))))

(defn delete
  "Delete the service"
  [item]
  (if-let [item (prepare-delete item)]
    (do (model.service/delete item)
        item)
    (throw+ "prepare delete failed")))

(defn fetch-xrd
  "Given a domain and an optional context uri. Attempts to find the xrd document"
  [domain url]
  {:pre [(instance? Domain domain)
         (or (nil? url)
             (string? url))]}
  (if (or (:http domain) (:https domain))
    (some->> (or (seq (util/path-segments url)) ["/"])
             (map #(str "https://" (:_id domain) % ".well-known/host-meta"))
             (concat (:hostMetaUri domain))
             (map fetch-xrd*)
             (filter identity)
             first)
    (timbre/warn "Domain does not have http(s) interface")))

(defn discover-statusnet-config
  "Fetch service's statusnet config. blocks"
  [domain url]
  (let [url (model.domain/statusnet-url domain)]
    (when-let [response (actions.resource/fetch url)]
      (when-let [sconfig (json/read-str (:body @response))]
        (model.domain/set-field! domain :statusnet-config sconfig)))))

(defn discover-capabilities
  [domain & [url]]
  (let [id (:_id domain)]
    (model.domain/set-field! domain :http  (util/socket-conectable? id 80))
    (model.domain/set-field! domain :https (util/socket-conectable? id 443))
    (model.domain/fetch-by-id (:_id domain))))

(defn set-links-from-xrd
  [domain xrd]
  (if-let [links (model.webfinger/get-links xrd)]
    (doseq [link links]
      (actions.domain/add-link domain link))
    (throw+ "Host meta does not have any links")))

(defn set-discovered!
  "marks the domain as having been discovered"
  [domain]
  {:pre [(instance? Domain domain)]}
  (model.domain/set-field! domain :discovered true)
  (model.domain/set-field! domain :discoveredAt (time/now))
  (let [id (:_id domain)
        domain (model.domain/fetch-by-id id)]
    (when-let [p (get @pending-discovers id)]
      (let [domain (model.domain/fetch-by-id (:_id domain))]
        (deliver p domain)))
    domain))

(defn discover-webfinger
  "Process the domain's webfinger record"
  ([^Domain domain]
   (discover-webfinger domain nil))
  ([^Domain domain url]
   {:pre [(instance? Domain domain)
          (or (nil? url)
              (string? url))]}
   (timbre/info "discover webfinger")
   (if-let [xrd (fetch-xrd domain url)]
     (do (set-links-from-xrd domain xrd)
         (set-discovered! domain)
         domain)
     (timbre/warnf "Could not get webfinger for domain: %s" (:_id domain)))))

(defn discover*
  [domain url]
  {:pre [(instance? Domain domain)
         (or (nil? url)
             (string? url))]}
  (d/chain
   (util/safe-task (discover-capabilities domain url))
   (fn [_]
     (let [domain (model.domain/fetch-by-id (:_id domain))]
       (util/safe-task (discover-webfinger domain url))
       #_(util/safe-task (discover-onesocialweb domain url))
       #_(util/safe-task (discover-statusnet-config domain url))))))

(defn discover
  [^Domain domain url]
  (if-not (:local domain)
    (do (timbre/debugf "discovering domain - %s" (:_id domain))
        (let [res (discover* domain url)]
          [(model.domain/fetch-by-id (:_id domain)) res]))
    (timbre/warn "local domains do not need to be discovered")))

(defn get-discovered
  [domain & [url options]]
  ;; (timbre/debugf "Getting discovered domain for: %s" (:_id domain))
  (if (:discovered domain)
    domain
    (let [id (:_id domain)
          p (dosync
             (when-not (get @pending-discovers id)
               (let [p (promise)]
                 (timbre/debug "Queuing discover")
                 (alter pending-discovers #(assoc % id p))
                 p)))
          p (if p
              (do
                (timbre/debug "discovering")
                @(second (discover domain url))
                p)
              (do
                (timbre/debug "using queued promise")
                (get @pending-discovers id)))]
      (or (deref p (lt/seconds 300) nil)
          (throw+ "Could not discover domain")))))
