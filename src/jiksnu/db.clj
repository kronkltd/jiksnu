(ns jiksnu.db
  (:use [ciste.config :only [config describe-config environment]])
  (:require [clojure.tools.logging :as log]
            [inflections.core :as inf]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.db :as db]))

;; Database functions

(defn drop-collection
  [^Class klass]
  (mc/remove (inf/plural (inf/underscore (.getSimpleName klass)))))

(defn drop-all!
  "Drop all collections"
  []
  (log/debug "dropping all collections")
  (db/drop-db))

(describe-config [:database :name]
  String
  "The name of the database to use")

(defn set-database!
  "Set the connection for mongo"
  []
  (log/info (str "setting database for " (environment)))
  ;; TODO: pass connection options
  (mg/connect!)
  (let [db (mg/get-db (config :database :name))]
    (mg/set-db! db)))

