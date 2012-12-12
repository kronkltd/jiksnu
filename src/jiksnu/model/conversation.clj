(ns jiksnu.model.conversation
  (:use [ciste.config :only [config]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [acceptance-of presence-of
                                      validation-set]])
  (:require [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.templates :as templates]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.query :as mq])
  (:import jiksnu.model.Conversation))

(def collection-name "conversations")
(def maker #'model/map->Conversation)
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

(def count-records (templates/make-counter    collection-name))
(def delete        (templates/make-deleter    collection-name))
(def drop!         (templates/make-dropper    collection-name))
(def set-field!    (templates/make-set-field! collection-name))

(defn fetch-all
  [& [params options]]
  (s/increment (str collection-name "_searched"))
  (let [sort-clause (mq/partial-query (mq/sort (:sort-clause options)))
        records (mq/with-collection collection-name
                  (mq/find params)
                  (merge sort-clause)
                  (mq/paginate :page (:page options 1)
                               :per-page (:page-size options 20)))]
    (map maker records)))

(defn fetch-by-id
  [id]
  (s/increment (str collection-name "_fetched"))
  (when-let [item (mc/find-map-by-id collection-name id)]
    (maker item)))

(defn create
  [params]
  (let [errors (create-validators params)]
    (if (empty? errors)
      (do
        (log/debugf "Creating conversation: %s" params)
        (s/increment (str collection-name "_created"))
        (mc/insert collection-name params)
        (fetch-by-id (:_id params)))
      (throw+ {:type :validation :errors errors}))))

(defn find-by-url
  [url]
  (fetch-all {:url url}))

(defn ensure-indexes
  []
  (doto collection-name
    (mc/ensure-index {:url 1} {:unique true})))
