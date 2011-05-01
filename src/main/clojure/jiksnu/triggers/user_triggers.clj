(ns jiksnu.triggers.user-triggers
  (:use ciste.config
        ciste.debug
        ciste.triggers
        clj-tigase.core
        jiksnu.actions.user-actions
        jiksnu.helpers.user-helpers
        jiksnu.namespace
        jiksnu.view)
  (:require [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.webfinger-actions :as actions.webfinger]
            [jiksnu.model.domain :as model.domain]))

(defn discover-user
  [action _ user]
  (let [domain (model.domain/show (:domain user))]
    (if (:xmpp domain)
      (request-vcard! user)
      (update-usermeta user))))

(defn fetch-updates-http
  [user]
  (let [uri (feed-link-uri user)]
    (actions.activity/fetch-remote-feed uri)))

(defn fetch-updates-xmpp
  [user]
  ;; TODO: send user timeline request
  (let [packet (make-packet
                {:to (make-jid user)
                 :from (make-jid "" (:domain (config)))
                 :type :get
                 :body (make-element
                        ["pubsub" {"xmlns" pubsub-uri}
                         ["items" {"node" microblog-uri}]])})]
    (deliver-packet! packet)))

(defn fetch-updates-trigger
  [action _ user]
  (let [domain (model.domain/show (:domain user))]
    (if (:xmpp domain)
      (fetch-updates-xmpp user)
      (fetch-updates-http user))))

(add-trigger! #'discover #'discover-user)
(add-trigger! #'fetch-updates #'fetch-updates-trigger)
