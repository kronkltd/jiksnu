(ns jiksnu.modules.core.model.activity
  (:require [clojure.java.io :as io]
            [jiksnu.db :as db]
            [jiksnu.modules.core.model :as model]
            [jiksnu.modules.core.model.user :as model.user]
            [jiksnu.modules.core.templates.model :as templates.model]
            [jiksnu.modules.core.validators :as vc]
            [jiksnu.session :as session]
            [monger.collection :as mc]
            [validateur.validation :as v])
  (:import org.bson.types.ObjectId
           org.joda.time.DateTime))

(defonce page-size 20)
(def collection-name "activities")
(def maker model/map->Activity)

(def create-validators
  (v/validation-set
   (vc/type-of :_id            ObjectId)
   (vc/type-of :id             String)
   (vc/type-of :title          String)
   (vc/type-of :content        String)
   (vc/type-of :verb           String)
   (vc/type-of [:object :type] String)
   (vc/type-of :local          Boolean)
   (vc/type-of :public         Boolean)
   (vc/type-of :author         String)
   #_(vc/type-of :update-source  ObjectId)
   #_(vc/type-of :conversation   ObjectId)
   (vc/type-of :created        DateTime)
   (vc/type-of :published      DateTime)
   (vc/type-of :updated        DateTime)))

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
  (mc/save (db/get-connection) collection-name activity))

(defn fetch-by-remote-id
  [id]
  (if-let [item (mc/find-one-as-map (db/get-connection) collection-name {:id id})]
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
    (mc/ensure-index (db/get-connection) {:id 1} {:unique true})
    (mc/ensure-index (db/get-connection) {:conversation 1})))
