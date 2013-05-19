(ns jiksnu.model.activity
  (:use [ciste.config :only [config]]
        [clojure.core.incubator :only [-?>]]
        [jiksnu.validators :only [type-of]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [validation-set presence-of]])
  (:require [clj-statsd :as s]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]
            [jiksnu.templates :as templates]
            [jiksnu.util :as util]
            [lamina.trace :as trace]
            [monger.collection :as mc]
            [monger.query :as mq])
  (:import jiksnu.model.Activity
           org.bson.types.ObjectId))

(defonce page-size 20)
(def collection-name "activities")
(def maker model/map->Activity)

(def create-probe (trace/probe-channel :activity:created))

(def create-validators
  (validation-set
   (type-of :_id                   ObjectId)
   (type-of :id                    String)
   (type-of :title                 String)
   (type-of :author                ObjectId)
   (type-of :content               String)
   (type-of :local                 Boolean)
   (type-of :public                Boolean)
   (type-of :update-source         ObjectId)
   (type-of [:object :object-type] String)
   (type-of :verb                  String)
   (type-of :conversation          ObjectId)

   ;; TODO: These should be joda times
   (presence-of   :created)
   (presence-of   :updated)
   ))

(defn fetch-by-id
  [id]
  ;; TODO: Should this always take a string?
  (let [id (if (string? id) (util/make-id id) id)]
    (s/increment (str collection-name " fetched"))
    (if-let [item (mc/find-map-by-id collection-name id)]
      (maker item))))

(def count-records (templates/make-counter    collection-name))
(def delete        (templates/make-deleter    collection-name))
(def drop!         (templates/make-dropper    collection-name))
(def set-field!    (templates/make-set-field! collection-name))
(def create        (templates/make-create     collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates/make-fetch-fn   collection-name maker))

(defn get-link
  [user rel content-type]
  (first (util/rel-filter rel (:links user) content-type)))

(defn get-author
  "Returns the user that is the author of this activity"
  [activity]
  (-?> activity
      :author
      model.user/fetch-by-id))

(defn get-comments
  [activity]
  (fetch-all {:parent (:_id activity)}
             {:sort [{:created 1}]}))

(defn author?
  [activity user]
  (= (:author activity) (:_id user)))

(defn update
  [activity]
  (s/increment "activities updated")
  (mc/save collection-name activity))

(defn privacy-filter
  [user]
  (if user
    (if (not (session/is-admin? user))
      {:$or [{:public true}
             {:author (:_id user)}]})
    {:public true}))

(defn fetch-by-remote-id
  [id]
  (if-let [item (mc/find-one-as-map collection-name {:id id})]
    (maker item)))

;; deprecated
(defn add-comment
  [parent comment]
  (s/increment "comment added")
  (mc/update collection-name
             {:_id (:_id parent)}
             {:$push {:comments (:_id comment)}}))

(defn parse-pictures
  [picture]
  (s/increment "pictures processed")
  (let [filename (:filename picture)
        tempfile (:tempfile picture)
        user-id (str (session/current-user-id))
        dest-file (io/file (str user-id "/" filename))]
    (when (and (not= filename "") tempfile)
      (.mkdirs (io/file user-id))
      (io/copy tempfile dest-file))))

(defn ensure-indexes
  []
  (doto collection-name
    (mc/ensure-index {:id 1} {:unique true})))
