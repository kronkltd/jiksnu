(ns jiksnu.templates.model
  (:use [slingshot.slingshot :only [throw+]])
  (:require [ciste.event :refer [defkey notify]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [inflections.core :as inf]
            jiksnu.db
            [jiksnu.namespace :as ns]
            [jiksnu.util :as util]
            [lamina.core :as l]
            [lamina.trace :as trace]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.query :as mq]
            monger.json)
  (:import java.io.FileNotFoundException
           java.text.SimpleDateFormat
           java.util.Date
           java.net.URL
           lamina.core.channel.Channel
           org.bson.types.ObjectId))

(defkey ::collection-dropped
  "Collections that were dropped")

(defkey ::item-deleted
  "Every item that is deleted")

(defkey ::item-set
  "when a field is set on an item")

;; index helpers

(defn make-fetch-fn
  [collection-name make-fn]
  (fn [& [params & [options]]]
    (let [sort-clause (mq/partial-query (mq/sort (:sort-clause options)))
          records (mq/with-collection collection-name
                    (mq/find params)
                    (merge sort-clause)
                    (mq/paginate :page (get options :page 1)
                                 :per-page (get options :page-size 20)))]
      (map make-fn records))))

(defkey ::collection-counted
  "when a collection is counted")

(defn make-counter
  [collection-name]
  (fn [& [params]]
    (let [params (or params {})]
      (let [n (mc/count collection-name params)]
        (notify ::collection-counted {:collection-name collection-name
                                      :count n
                                      :params params})
        n))))

(defn make-deleter
  [collection-name]
  (fn [item]
    (mc/remove-by-id collection-name (:_id item))
    (notify ::item-deleted {:item item
                            :collection collection-name})
    item))

(defn make-dropper
  [collection-name]
  (fn []
    (mc/remove collection-name)
    (notify ::collection-dropped {:collection collection-name})
    nil))

(defn make-set-field!
  [collection-name]
  (fn [item field value]
    (if (not= field :links)
      (when-not (= (get item field) value)
        (notify ::item-set
                {:item item
                 :field field
                 :value value})
        (mc/update collection-name
                   {:_id (:_id item)}
                   {:$set {field value}}))
      (throw+ "can not set links values"))))

(defn make-remove-field!
  [collection-name]
  (fn [item field]
    (trace/trace* (str collection-name ":field:remove") [item field])
    (mc/update collection-name
               {:_id (:_id item)}
               {:$unset {field 1}})))

(defn make-create
  [collection-name fetcher validator]
  (fn [params]
    (let [errors (validator params)]
      (if (empty? errors)
        (do
          (let [name (str collection-name ":create:in")]
            (trace/trace* name {:name name :params params}))
          (mc/insert collection-name params)
          (let [item (fetcher (:_id params))]
            (trace/trace* (str collection-name ":created") item)
            item))
        (throw+ {:type :validation :errors errors})))))

(defn make-fetch-by-id
  ([collection-name maker]
     (make-fetch-by-id collection-name maker true))
  ([collection-name maker convert-id]
     (fn [id]
       (let [id (if (and convert-id (string? id))
                  (util/make-id id) id)]
         (trace/trace* (str collection-name ":fetched") id)
         (when-let [item (mc/find-map-by-id collection-name id)]
           (maker item))))))

(defn make-push-value!
  [collection-name]
  (fn [item key value]
    (mc/update collection-name
               (select-keys item #{:_id})
               {:$push {key value}})))

(defn make-pop-value!
  [collection-name]
  (fn [item key value]
    (mc/update collection-name
               (select-keys item #{:_id})
               {:$pop {key value}})))
