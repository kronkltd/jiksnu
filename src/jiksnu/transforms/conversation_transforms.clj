(ns jiksnu.transforms.conversation-transforms
  (:use [ciste.config :only [config]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.route-one.core :as r]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.routes.helpers :as rh]
            [jiksnu.util :as util]
            [lamina.trace :as trace])
  (:import java.net.URI))

(defn local-url?
  [url]
  (= (config :domain)
     (util/get-domain-name url)))

(defn set-update-source
  [item]
  (if (:update-source item)
    item
    (if-let [url (:url item)]
      (if-let [source (if (local-url? url)
                        (let [atom-url (rh/formatted-url "show conversation"
                                                         {:id (:_id item)} "atom")]
                          (actions.feed-source/find-or-create {:topic atom-url}))
                        (try
                          (actions.feed-source/discover-source url)
                          (catch RuntimeException ex
                            (trace/trace :errors:handled ex))))]
        (assoc item :update-source (:_id source))
        (throw+ "could not determine source"))
      (throw+ "Could not determine url"))))

(defn set-url
  [item]
  (if (:url item)
    item
    (when (:local item)
      (assoc item :url (r/named-url "show conversation" {:id (:_id item)})))))

(defn set-domain
  [item]
  (if (:domain item)
    item
    (if-let [domain (if (:local item)
                      (actions.domain/current-domain)
                      (when-let [uri (URI. (:url item))]
                        (when-let [domain-name (.getHost uri)]
                          (actions.domain/find-or-create {:_id domain-name}))))]
      (assoc item :domain (:_id domain))
      (throw+ "Could not determine domain"))))