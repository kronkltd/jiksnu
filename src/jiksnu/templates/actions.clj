(ns jiksnu.templates.actions
  (:require [jiksnu.channels :as ch]
            [jiksnu.db :refer [_db]]
            [manifold.bus :as bus]
            [monger.collection :as mc]
            monger.json
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre]))

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
      {:items (map :_id records)
       :page page
       :page-size page-size
       :totalItems record-count
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

(defn make-add-link*
  [collection-name]
  (fn [item link]
    (bus/publish! ch/events (str collection-name ":linkAdded") [item link])
    (mc/update @_db collection-name
      (select-keys item #{:_id})
      {:$addToSet {:links link}})
    item))

(defn make-delete
  [delete-fn access-fn]
  (fn[item]
    (timbre/debug "Deleting item")
    (if (access-fn item)
      (delete-fn item)
      (throw+ {:type :authorization
               :msg "You are not authorized to delete that object"}))))
