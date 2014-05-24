(ns jiksnu.templates.model
  (:use [slingshot.slingshot :only [throw+]])
  (:require [clj-statsd :as s]
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

;; index helpers

(defn make-fetch-fn
  [collection-name make-fn]
  (trace/instrument
   (fn [& [params & [options]]]
     (let [sort-clause (mq/partial-query (mq/sort (:sort-clause options)))
           records (mq/with-collection collection-name
                     (mq/find params)
                     (merge sort-clause)
                     (mq/paginate :page (get options :page 1)
                                  :per-page (get options :page-size 20)))]
       (map make-fn records)))
   {:name (keyword (str collection-name ":searcher"))}))

(defn make-counter
  [collection-name]
  (trace/instrument
   (fn [& [params]]
      (let [params (or params {})]
        (trace/trace* (str collection-name ":counted") 1)
        (mc/count collection-name params)))
   {:name (keyword (str collection-name ":counter"))}))

(defn make-deleter
  [collection-name]
  (fn [item]
    (trace/trace* (str collection-name ":deleted") item)
    (mc/remove-by-id collection-name (:_id item))
    item))

(defn make-dropper
  [collection-name]
  (fn []
    (trace/trace* (str collection-name ":dropped") collection-name)
    (mc/remove collection-name)))

(defn make-set-field!
  [collection-name]
  (trace/instrument
   (fn [item field value]
     (if (not= field :links)
       (when-not (= (get item field) value)
         (trace/trace* (str collection-name ":field:set") [item field value])
         (mc/update collection-name
           {:_id (:_id item)}
           {:$set {field value}}))
       (throw+ "can not set links values")))
   {:name (keyword (str collection-name ":setter"))}))

(defn make-remove-field!
  [collection-name]
  (trace/instrument
   (fn [item field]
     (trace/trace* (str collection-name ":field:remove") [item field])
     (mc/update collection-name
       {:_id (:_id item)}
       {:$unset {field 1}}))
   {:name (keyword (str collection-name ":unsetter"))}))

(defn make-create
  [collection-name fetcher validator]
  (trace/instrument
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
         (throw+ {:type :validation :errors errors}))))
   {:name (keyword (str collection-name ":creator"))}))

(defn make-fetch-by-id
  ([collection-name maker]
     (make-fetch-by-id collection-name maker true))
  ([collection-name maker convert-id]
     (trace/instrument
      (fn [id]
        (let [id (if (and convert-id (string? id))
                   (util/make-id id) id)]
          (trace/trace* (str collection-name ":fetched") id)
          (when-let [item (mc/find-map-by-id collection-name id)]
            (maker item))))
      {:name (keyword (str collection-name ":fetcher"))})))

(defn make-push-value!
  [collection-name]
  (trace/instrument
   (fn [item key value]
     (mc/update collection-name
                (select-keys item #{:_id})
                {:$push {key value}}))
   {:name (keyword (str collection-name ":pusher"))}))

(defn make-pop-value!
  [collection-name]
  (trace/instrument
   (fn [item key value]
     (mc/update collection-name
                (select-keys item #{:_id})
                {:$pop {key value}}))
   {:name (keyword (str collection-name ":popper"))}))
