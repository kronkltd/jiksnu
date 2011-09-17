(ns jiksnu.triggers.user-triggers
  (:use (ciste config debug triggers)
        (jiksnu namespace view)
        lamina.core)
  (:require (clj-tigase [core :as tigase]
                        [element :as element])
            (clojure.tools [logging :as log])
            (jiksnu.actions [activity-actions :as actions.activity]
                            [webfinger-actions :as actions.webfinger]
                            [user-actions :as actions.user]
                            [webfinger-actions :as actions.webfinger])
            (jiksnu.helpers [user-helpers :as helpers.user])
            (jiksnu.model [domain :as model.domain]))
  (:import org.deri.any23.Any23))

(defonce a23 (Any23.))

(defn discover-user-xmpp
  [user]
  (println "discover xmpp")
  (actions.user/request-vcard! user))

(defn discover-user-http
  [user]
  (println "discovering http")
  (actions.webfinger/update-usermeta user)
  #_(request-hcard user))

(defn discover-user
  [action _ user]
  (let [domain (model.domain/show (:domain user))]
    (if (:discovered domain)
      (do (async (discover-user-xmpp user))
          (async (discover-user-http user)))
      (actions.user/enqueue-discover user))))

(defn fetch-updates-http
  [user]
  (let [uri (helpers.user/feed-link-uri user)]
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
    (tigase/deliver-packet! packet)))

(defn fetch-updates-trigger
  [action _ user]
  (let [domain (model.domain/show (:domain user))]
    (if (:xmpp domain)
      (fetch-updates-xmpp user)
      (fetch-updates-http user))))

(defn create-trigger
  [action _ user]
  (actions.user/discover user))

(add-trigger! #'actions.user/create        #'create-trigger)
(add-trigger! #'actions.user/discover      #'discover-user)
(add-trigger! #'actions.user/fetch-updates #'fetch-updates-trigger)
