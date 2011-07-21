(ns jiksnu.triggers.user-triggers
  (:use ciste.config
        ciste.debug
        ciste.triggers
        jiksnu.actions.user-actions
        jiksnu.helpers.user-helpers
        jiksnu.namespace
        jiksnu.view
        lamina.core)
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.webfinger-actions :as actions.webfinger]
            [jiksnu.model.domain :as model.domain])
  (:import org.deri.any23.Any23))

(defonce a23 (Any23.))

(defn discover-user-xmpp
  [user]
  (println "discover xmpp")
  (request-vcard! user))

(defn discover-user-http
  [user]
  (println "discovering http")
  (update-usermeta user)
  #_(request-hcard user))

(defn discover-user
  [action _ user]
  (let [domain (model.domain/show (:domain user))]
    (if (:discovered domain)
      (do (async (discover-user-xmpp user))
          (async (discover-user-http user)))
      (enqueue-discover user))))

(defn fetch-updates-http
  [user]
  (let [uri (feed-link-uri user)]
    (actions.activity/fetch-remote-feed uri)))

(defn fetch-updates-xmpp
  [user]
  ;; TODO: send user timeline request
  (let [packet (tigase/make-packet
                {:to (tigase/make-jid user)
                 :from (tigase/make-jid "" (config :domain))
                 :type :get
                 :body (element/make-element
                        ["pubsub" {"xmlns" pubsub-uri}
                         ["items" {"node" microblog-uri}]])})]
    (tigase/deliver-packet! (spy packet))))

(defn fetch-updates-trigger
  [action _ user]
  (let [domain (model.domain/show (:domain user))]
    (if (:xmpp domain)
      (fetch-updates-xmpp user)
      (fetch-updates-http user))))

(defn create-trigger
  [action _ user]
  (discover user))

(add-trigger! #'create #'create-trigger)
(add-trigger! #'discover #'discover-user)
(add-trigger! #'fetch-updates #'fetch-updates-trigger)
