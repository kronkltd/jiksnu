(ns jiksnu.templates
  (:use [slingshot.slingshot :only [throw+]])
  (:require [clj-statsd :as s]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [inflections.core :as inf]
            [jiksnu.namespace :as ns]
            [jiksnu.util :as util
             ]
            [lamina.core :as l]
            [lamina.trace :as trace]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.query :as mq]
            monger.json
            [plaza.rdf.core :as rdf]
            [plaza.rdf.implementations.jena :as jena])
  (:import java.io.FileNotFoundException
           java.text.SimpleDateFormat
           java.util.Date
           java.net.URL
           lamina.core.channel.Channel
           org.bson.types.ObjectId))

;; index helpers

(defn make-fetch-fn
  [collection-name make-fn]
  (fn [& [params & [options]]]
    (s/increment (str collection-name " searched"))
    (let [sort-clause (mq/partial-query (mq/sort (:sort-clause options)))
          records (mq/with-collection collection-name
                    (mq/find params)
                    (merge sort-clause)
                    (mq/paginate :page (:page options 1)
                                 :per-page (:page-size options 20)))]
      (map make-fn records))))

(defn make-indexer*
  [{:keys [page-size sort-clause count-fn fetch-fn]}]
  (fn [& [{:as params} & [{:as options} & _]]]
    (let [options (or options {})
          page (get options :page 1)
          criteria {:sort-clause (or (:sort-clause options)
                                     sort-clause)
                    :page page
                    :page-size page-size
                    :skip (* (dec page) page-size)
                    :limit page-size}
          record-count (count-fn params)
          records (fetch-fn params criteria)]
      {:items records
       :page page
       :page-size page-size
       :totalRecords record-count
       :args options})))

(defmacro make-indexer
  [namespace-sym & options]
  (let [options (apply hash-map options)]
    `(do (require ~namespace-sym)
         (let [ns-ns# (the-ns ~namespace-sym)]
           (if-let [count-fn# (ns-resolve ns-ns# (symbol "count-records"))]
             (if-let [fetch-fn# (ns-resolve ns-ns# (symbol "fetch-all" ))]
               (make-indexer*
                {:sort-clause (get ~options :sort-clause {:updated -1})
                 :page-size (get ~options :page-size 20)
                 :fetch-fn fetch-fn#
                 :count-fn count-fn#})
               (throw+ "Could not find fetch function"))
             (throw+ "Could not find count function"))))))

(defn make-counter
  [collection-name]
  (fn [& [params]]
     (let [params (or params {})]
       (trace/trace* (str collection-name ":counted") 1)
       (s/increment (str collection-name " counted"))
       (mc/count collection-name params))))

(defn make-deleter
  [collection-name]
  (fn [item]
    (trace/trace* (str collection-name ":deleted") item)
    (s/increment (str collection-name " deleted"))
    (mc/remove-by-id collection-name (:_id item))
    item))

(defn make-dropper
  [collection-name]
  (fn []
    (trace/trace* (str collection-name ":dropped") collection-name)
    (mc/remove collection-name)))

(defn make-set-field!
  [collection-name]
  (fn [item field value]
    (if (not= field :links)
      (when-not (= (get item field) value)
        (log/debugf "setting %s(%s): (%s = %s)" collection-name (:_id item) field (pr-str value))
        (s/increment (str collection-name " field set"))
        (mc/update collection-name
          {:_id (:_id item)}
          {:$set {field value}}))
      (throw+ "can not set links values"))))

(defn make-add-link*
  [collection-name]
  (fn [item link]
    (trace/trace* (str collection-name ":linkAdded") item)
    (mc/update collection-name
      (select-keys item #{:_id})
      {:$addToSet {:links link}})
    item))

(defn make-create
  [collection-name fetcher validator]
  (fn [params]
    (let [errors (validator params)]
      (if (empty? errors)
        (do
          (log/debugf "Creating %s: %s" collection-name (pr-str params))
          (mc/insert collection-name params)
          (let [item (fetcher (:_id params))]
            (trace/trace* (str collection-name ":created") item)
            (s/increment (str collection-name "_created"))
            item))
        (throw+ {:type :validation :errors errors})))))

(defn make-fetch-by-id
  ([collection-name maker]
     (make-fetch-by-id collection-name maker true))
  ([collection-name maker convert-id]
     (fn [id]
       (let [id (if (and convert-id (instance? String id))
                  (util/make-id id) id)]
         (log/debugf "fetching %s(%s)" collection-name id)
         (trace/trace* (str collection-name ":fetched") id)
         (s/increment (str collection-name "_fetched"))
         (when-let [item (mc/find-map-by-id collection-name id)]
           (maker item))))))

