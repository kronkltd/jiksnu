(ns jiksnu.model.activity
  (:require [clojure.java.io :as io]
            [jiksnu.db :refer [_db]]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]
            [jiksnu.templates.model :as templates.model]
            [jiksnu.util :as util]
            [jiksnu.validators :refer [type-of]]
            [monger.collection :as mc]
            [validateur.validation :refer [validation-set presence-of]])
  (:import org.bson.types.ObjectId
           org.joda.time.DateTime))

(defonce page-size 20)
(def collection-name "activities")
(def maker model/map->Activity)

(def create-validators
  (validation-set
   (type-of :_id                   ObjectId)

   (type-of :id                    String)
   (type-of :title                 String)
   (type-of :content               String)
   (type-of :verb                  String)
   (type-of [:object :type]        String)

   (type-of :local                 Boolean)
   (type-of :public                Boolean)

   (type-of :author                String)
   ;; (type-of :update-source         ObjectId)
   ;; (type-of :conversation          ObjectId)

   ;; (presence-of :created)
   (type-of :created               DateTime)
   ;; (presence-of :published)
   (type-of :published             DateTime)
   ;; (presence-of :updated)
   (type-of :updated               DateTime)
   ))

(def count-records (templates.model/make-counter       collection-name))
(def delete        (templates.model/make-deleter       collection-name))
(def drop!         (templates.model/make-dropper       collection-name))
(def remove-field! (templates.model/make-remove-field! collection-name))
(def set-field!    (templates.model/make-set-field!    collection-name))
(def fetch-by-id   (templates.model/make-fetch-by-id   collection-name maker))
(def create        (templates.model/make-create        collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates.model/make-fetch-fn      collection-name maker))

(defn author?
  [activity user]
  (= (:author activity) (:_id user)))

(defn privacy-filter
  [user]
  (if user
    (if (not (session/is-admin? user))
      {:$or [{:public true}
             {:author (:_id user)}]})
    {:public true}))

(defn get-link
  [user rel content-type]
  (first (util/rel-filter rel (:links user) content-type)))

(defn get-author
  "Returns the user that is the author of this activity"
  [activity]
  (some-> activity
          :author
          model.user/fetch-by-id))

(defn fetch-comments
  [activity]
  (fetch-all {:parent (:_id activity)}
             {:sort [{:created 1}]}))

(defn update-record
  [activity]
  (mc/save @_db collection-name activity))

(defn fetch-by-remote-id
  [id]
  (if-let [item (mc/find-one-as-map @_db collection-name {:id id})]
    (maker item)))

(defn parse-pictures
  [picture]
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
    (mc/ensure-index @_db {:id 1} {:unique true})
    (mc/ensure-index @_db {:conversation 1})))
