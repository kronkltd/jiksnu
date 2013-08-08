(ns jiksnu.templates.actions
  (:use [slingshot.slingshot :only [throw+]])
  (:require [clj-statsd :as s]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [inflections.core :as inf]
            [jiksnu.namespace :as ns]
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

(defn make-indexer*
  [{:keys [page-size sort-clause count-fn fetch-fn]}]
  (trace/instrument
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
        :args options}))
   {:name :indexer}))

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

(defn make-add-link*
  [collection-name]
  (fn [item link]
    (trace/trace* (str collection-name ":linkAdded") [item link])
    (mc/update collection-name
      (select-keys item #{:_id})
      {:$addToSet {:links link}})
    item))

