(ns jiksnu.actions.service-actions
  (:require [ciste.config :refer [config]]
            [ciste.core :refer [defaction]]
            [ciste.initializer :refer [definitializer]]
            [ciste.model :as cm]
            [clj-time.core :as time]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.ops :as ops]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.domain-transforms :as transforms.domain]
            [jiksnu.util :as util]
            [lamina.core :as l]
            [lamina.time :as lt]
            [slingshot.slingshot :refer [throw+ try+]])
  (:import java.net.URL
           jiksnu.model.Domain))

(defn fetch-xrd*
  [url]
  {:pre [(string? url)]}
  (try+
   (let [res (ops/update-resource url {:force true})]
     (l/on-realized res
                    (fn [_] (log/info "Finished fetching xrd"))
                    (fn [_] (log/error "Fetching xrd caused error")))

     (let [response @res]
       (when (= 200 (:status response))
         (try
           (if-let [body (:body response)]
             (cm/string->document body))
           (catch RuntimeException ex
             (log/error "Fetching host meta failed" ex))))))
   (catch Object ex
     (log/error ex))))

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
    (log/warn "Domain does not have http(s) interface")))

(defn discover-statusnet-config
  [domain url]
  (let [urls (log/spy :info (model.domain/statusnet-url domain))]
    (->> [urls]
         (map (fn [url]
                (when-let [response (ops/update-resource url)]
                  (when-let [sconfig (json/read-str (:body @response))]
                    (model.domain/set-field! domain :statusnet-config sconfig)))))
         (filter identity)
         first)))

(defn discover-capabilities
  [domain & [url]]
  (let [id (:_id domain)]
    (model.domain/set-field! domain :http  (util/socket-conectable? id 80))
    (model.domain/set-field! domain :https (util/socket-conectable? id 443))
    (model.domain/fetch-by-id (:_id domain))))

(defn discover*
  [domain url]
  {:pre [(instance? Domain domain)
         (or (nil? url)
             (string? url))]}
  (l/run-pipeline
   (util/safe-task (discover-capabilities domain url))
   (fn [_]
     (let [domain (model.domain/fetch-by-id (:_id domain))]
       (l/merge-results
        (util/safe-task (discover-webfinger domain url))
        ;; (util/safe-task (discover-onesocialweb domain url))
        ;; (util/safe-task (discover-statusnet-config domain url))
        )))))

(defaction discover
  [^Domain domain url]
  (if-not (:local domain)
    (do (log/debugf "discovering domain - %s" (:_id domain))
        (let [res (discover* domain url)]
          [(model.domain/fetch-by-id (:_id domain)) res]))
    (log/warn "local domains do not need to be discovered")))

(defn get-discovered
  [domain & [url options]]
  ;; (log/debugf "Getting discovered domain for: %s" (:_id domain))
  (if (:discovered domain)
    domain
    (let [id (:_id domain)
          p (dosync
             (when-not (get @pending-discovers id)
               (let [p (promise)]
                 (log/debug "Queuing discover")
                 (alter pending-discovers #(assoc % id p))
                 p)))
          p (if p
              (do
                (log/debug "discovering")
                @(second (discover domain url options))
                p)
              (do
                (log/debug "using queued promise")
                (get @pending-discovers id)))]
      (or (deref p (lt/seconds 300) nil)
          (throw+ "Could not discover domain")))))

(defn discover-webfinger
  [^Domain domain url]
  {:pre [(instance? Domain domain)
         (or (nil? url)
             (string? url))]}
  (log/info "discover webfinger")
  (if-let [xrd (fetch-xrd domain url)]
    (do (set-links-from-xrd domain xrd)
        (set-discovered! domain)
        domain)
    (log/warnf "Could not get webfinger for domain: %s" (:_id domain))))

