(ns jiksnu.model
  (:use ciste.core
        [ciste.config :only [config environment]]
        [ciste.initializer :only [definitializer]]
        [clj-factory.core :only [factory]]
        [clojurewerkz.route-one.core :only [*base-url*]]
        [clojure.core.incubator :only [-?> -?>>]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clj-statsd :as s]
            [clojure.string :as string]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [inflections.core :as inf]
            [jiksnu.db :as db]
            [jiksnu.namespace :as ns]
            [jiksnu.util :as util]
            [lamina.core :as l]
            [lamina.time :as time]
            [lamina.trace :as trace]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.query :as mq]
            monger.joda-time
            monger.json
            [plaza.rdf.core :as rdf]
            [plaza.rdf.implementations.jena :as jena])
  (:import com.mongodb.WriteConcern
           com.ocpsoft.pretty.time.PrettyTime
           java.io.FileNotFoundException
           java.io.PrintWriter
           java.text.SimpleDateFormat
           java.util.Date
           java.net.URL
           lamina.core.channel.Channel
           org.bson.types.ObjectId
           org.joda.time.DateTime
           java.io.StringReader))

;; TODO: pull these from ns/
(defonce bound-ns {:hm "http://host-meta.net/xrd/1.0"
                   :xrd "http://docs.oasis-open.org/ns/xri/xrd-1.0"})

(defrecord Activity                [])
(defrecord AuthenticationMechanism [])
(defrecord Conversation            [])
(defrecord Domain                  [])
(defrecord FeedSource              [])
(defrecord FeedSubscription        [])
(defrecord Group                   [])
(defrecord Item                    [])
(defrecord Key                     [])
(defrecord Like                    [])
(defrecord Resource                [])
(defrecord Subscription            [])
(defrecord User                    [])

(def entity-names
  [
   Activity
   AuthenticationMechanism
   Conversation
   Domain
   FeedSource
   FeedSubscription
   Group
   Item
   Key
   Like
   Resource
   Subscription
   User
   ]
  )

;; Entity predicates

(defn activity?
  [activity]
  (instance? Activity activity))

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

