(ns jiksnu.model.conversation
  (:use [ciste.config :only [config]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [validation-set presence-of]])
  (:require [clojure.tools.logging :as log]
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
   (presence-of   :created)
   (presence-of   :updated)

   ))

(defn count-records
  ([] (count-records {}))
  ([params]
     (mc/count collection-name params)))

(declare fetch-by-id)

(defn create
  [record]
  (let [errors (create-validators record)]
    (if (empty? errors)
      (do
        (log/debugf "Creating conversation: %s" record)
        (mc/insert collection-name record)
        (fetch-by-id (:_id record)))
      (throw+ {:type :validation :errors errors}))))

(defn delete
  [record]
  (let [record (fetch-by-id (:_id record))]
    (do
      (log/debug "deleting conversation")
      (mc/remove collection-name record))
    record))

(defn drop!
  []
  (mc/remove collection-name))

(defn fetch-by-id
  [id]
  (if-let [conversation (mc/find-map-by-id collection-name id)]
    (model/map->Conversation conversation)
    (log/warnf "Could not find conversation: %s" id)))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (let [sort-clause (mq/partial-query (mq/sort (:sort-clause options)))
           records (mq/with-collection collection-name
                     (mq/find params)
                     (merge sort-clause)
                     (mq/paginate :page (:page options 1)
                                  :per-page (:page-size options 20)))]
       (map model/map->Conversation records))))

