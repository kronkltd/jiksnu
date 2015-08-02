(ns jiksnu.db
  (:require [ciste.config :refer [config describe-config environment]]
            [clojure.tools.logging :as log]
            [inflections.core :as inf]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.db :as db])
  (:import com.mongodb.WriteConcern))

(def _db (ref nil))
(def _conn (ref nil))

;; Database functions

(defn drop-collection
  [^Class klass]
  (mc/remove @_db (inf/plural (inf/underscore (.getSimpleName klass)))))

(defn drop-all!
  "Drop all collections"
  []
  (log/info "dropping all collections")
  (db/drop-db @_db))

(describe-config [:database :name]
                 String
                 "The name of the database to use")

(defn set-database!
  "Set the connection for mongo"
  []
  (log/info (str "setting database for " (environment)))
  (mg/set-default-write-concern! WriteConcern/FSYNC_SAFE)
  ;; TODO: pass connection options
  (let [conn (mg/connect)
        db (mg/get-db conn (config :database :name))]
    (dosync
     (ref-set _conn conn)
     (ref-set _db db))))

