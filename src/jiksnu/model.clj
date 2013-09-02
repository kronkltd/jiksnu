(ns jiksnu.model
  (:use [ciste.config :only [config]]
        [ciste.initializer :only [definitializer]]
        [clojurewerkz.route-one.core :only [*base-url*]])
  (:require [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.db :as db]
            [monger.core :as mg])
  (:import com.mongodb.WriteConcern))

;; TODO: pull these from ns/
(defonce bound-ns {:hm "http://host-meta.net/xrd/1.0"
                   :xrd "http://docs.oasis-open.org/ns/xri/xrd-1.0"})

(defrecord Activity                [])
(defrecord AuthenticationMechanism [])
(defrecord Client                  [])
(defrecord Conversation            [])
(defrecord Dialback                [])
(defrecord Domain                  [])
(defrecord FeedSource              [])
(defrecord FeedSubscription        [])
(defrecord Group                   [])
(defrecord GroupMembership         [])
(defrecord Item                    [])
(defrecord Key                     [])
(defrecord Like                    [])
(defrecord RequestToken            [])
(defrecord Resource                [])
(defrecord Stream                  [])
(defrecord Subscription            [])
(defrecord User                    [])

(def entity-names
  [
   Activity
   AuthenticationMechanism
   Client
   Conversation
   Dialback
   Domain
   FeedSource
   FeedSubscription
   Group
   GroupMembership
   Item
   Key
   Like
   RequestToken
   Resource
   Stream
   Subscription
   User
   ]
  )

;; Entity predicates

(defn domain?
  [domain]
  (instance? Domain domain))

(defn subscription?
  [subscription]
  (instance? Subscription subscription))

(defn user?
  "Is the provided object a user?"
  [user] (instance? User user))

;; initializer

(definitializer
  (let [url (format "http://%s" (config :domain))]
    (alter-var-root #'*base-url*
                    (constantly url)))

  (s/setup "localhost" 8125)

  (db/set-database!)

  (mg/set-default-write-concern! WriteConcern/FSYNC_SAFE)
  )

