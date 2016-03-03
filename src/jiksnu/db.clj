(ns jiksnu.db
  (:require [environ.core :refer [env]]
            [inflections.core :as inf]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.db :as db]
            [taoensso.timbre :as timbre])
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
  (timbre/info "dropping all collections")
  (db/drop-db @_db))

(defn set-database!
  "Set the connection for mongo"
  []
  (mg/set-default-write-concern! WriteConcern/FSYNC_SAFE)
  (timbre/infof "Connecting to %s" (env :jiksnu-database-url))
  ;; TODO: pass connection options
  (let [{:keys [conn db]} (mg/connect-via-uri (env :jiksnu-db-url))]
    (dosync
     (ref-set _conn conn)
     (ref-set _db db))))
