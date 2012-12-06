(ns jiksnu.db
  (:use [ciste.config :only [config environment]]
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
            [jiksnu.namespace :as ns]
            [lamina.core :as l]
            [lamina.time :as time]
            [lamina.trace :as trace]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.query :as mq]
            monger.joda-time
            monger.json
            )
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

;; Database functions

(defn drop-collection
  [klass]
  (mc/remove (inf/plural (inf/underscore (.getSimpleName klass)))))

(defn drop-all!
  "Drop all collections"
  []
  (log/debug "dropping all collections")
  (doseq [entity [] #_entity-names]
    (log/debugf "dropping %s" entity)
    (drop-collection entity)))

(defn set-database!
  "Set the connection for mongo"
  []
  (log/info (str "setting database for " (environment)))
  ;; TODO: pass connection options
  (mg/connect!)
  (let [db (mg/get-db (str (config :database :name)))]
    (mg/set-db! db)))

