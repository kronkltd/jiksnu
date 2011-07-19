(ns jiksnu.triggers.user-triggers
  (:use ciste.config
        ciste.debug
        ciste.triggers
        clj-tigase.core
        jiksnu.actions.user-actions
        jiksnu.helpers.user-helpers
        jiksnu.namespace
        jiksnu.view)
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.webfinger-actions :as actions.webfinger]
            [jiksnu.model.domain :as model.domain]))

(defn discover-user-xmpp
  [user]
  (println "discover xmpp")
  (request-vcard! user))

(defn discover-user-http
  [user]
  (println "discovering http")
  (update-usermeta user)
  #_(request-hcard user)
  )

(defn discover-user
  [action _ user]
  (let [domain (model.domain/show (:domain user))]
    ((if (:discovered domain)
       (if (:xmpp domain) discover-user-xmpp discover-user-http)
       enqueue-discover)
     user)))

(defn fetch-updates-http
  [user]
  (let [uri (feed-link-uri user)]
    (actions.activity/fetch-remote-feed uri)))

(defn fetch-updates-xmpp
  [user]
  ;; TODO: send user timeline request
  (let [packet (make-packet
                {:to (make-jid user)
                 :from (make-jid "" (config :domain))
                 :type :get
                 :body (make-element
                        ["pubsub" {"xmlns" pubsub-uri}
                         ["items" {"node" microblog-uri}]])})]
    (deliver-packet! (spy packet))))

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
