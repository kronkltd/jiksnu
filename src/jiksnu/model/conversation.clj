(ns jiksnu.model.conversation
  (:use [ciste.config :only [config]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [acceptance-of presence-of validation-set]])
  (:require [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.query :as mq])
  (:import jiksnu.model.Conversation))

(def collection-name "conversations")
(def default-page-size 20)

(def create-validators
  (validation-set
   (presence-of :_id)
   (presence-of :url)
   (presence-of :created)
   (presence-of :updated)
   (presence-of :domain)
   (acceptance-of :local :accept (partial instance? Boolean))
   (presence-of   :update-source)))

(defn count-records
  ([] (count-records {}))
  ([params]
     (s/increment "conversations_counted")
     (mc/count collection-name params)))

(defn fetch-by-id
  [id]
  (s/increment "conversations_fetched")
  (if-let [conversation (mc/find-map-by-id collection-name id)]
    (model/map->Conversation conversation)
    (log/warnf "Could not find conversation: %s" id)))

(defn create
  [record]
  (let [errors (create-validators record)]
    (if (empty? errors)
      (do
        (log/debugf "Creating conversation: %s" record)
        (s/increment "conversations_created")
        (mc/insert collection-name record)
        (fetch-by-id (:_id record)))
      (throw+ {:type :validation :errors errors}))))

(defn delete
  [record]
  (let [record (fetch-by-id (:_id record))]
    (do
      (log/debug "deleting conversation")
      (s/increment "conversations_deleted")
      (mc/remove collection-name record))
    record))

(defn drop!
  []
  (s/increment "conversations_dropped")
  (mc/remove collection-name))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (s/increment "conversations_searched")
     (let [sort-clause (mq/partial-query (mq/sort (:sort-clause options)))
           records (mq/with-collection collection-name
                     (mq/find params)
                     (merge sort-clause)
                     (mq/paginate :page (:page options 1)
                                  :per-page (:page-size options 20)))]
       (map model/map->Conversation records))))

(defn find-by-url
  [url]
  (fetch-all {:url url}))
