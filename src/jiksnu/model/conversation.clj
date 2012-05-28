(ns jiksnu.model.conversation
  (:use [ciste.config :only [config]]
        [ciste.debug :only [spy]])
  (:require [clojure.tools.logging :as log]
            [karras.entity :as entity])
  (:import jiksnu.model.Conversation))

(defn drop!
  []
  (entity/delete-all Conversation))

(defn fetch-by-id
  [id]
  (entity/fetch-by-id Conversation id))

(defn delete
  [record]
  (let [record (fetch-by-id (:_id record))]
    (do
      (log/debug "deleting conversation")
      (entity/delete record))
    record))

(defn fetch-all
  ([]
     (fetch-all {} {}))
  ([params]
     (fetch-all params {}))
  ([params opts]
     (apply entity/fetch-all Conversation params opts)))

(defn create
  [record]
  (log/debugf "Creating conversation: %s" (:_id record))
  (entity/create Conversation record))

(defn count-records
  ([] (count-records {}))
  ([params]
     (entity/count-instances Conversation params)))
