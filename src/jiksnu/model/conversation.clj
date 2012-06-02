(ns jiksnu.model.conversation
  (:use [ciste.config :only [config]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [monger.collection :as mc]
            [monger.core :as mg])
  (:import jiksnu.model.Conversation))

(def collection-name "conversations")

(defn drop!
  []
  (mc/remove collection-name))

(defn fetch-by-id
  [id]
  (mc/find-map-by-id collection-name id))

(defn delete
  [record]
  (let [record (fetch-by-id (:_id record))]
    (do
      (log/debug "deleting conversation")
      (mc/remove collection-name record))
    record))

(defn fetch-all
  ([]
     (fetch-all {} {}))
  ([params]
     (fetch-all params {}))
  ([params opts]
     (mc/find-maps collection-name params)))

(defn create
  [record]
  (log/debugf "Creating conversation: %s" (:_id record))
  (mc/insert collection-name record))

(defn count-records
  ([] (count-records {}))
  ([params]
     (mc/count collection-name params)))
