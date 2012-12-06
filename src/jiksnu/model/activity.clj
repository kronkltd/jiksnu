(ns jiksnu.model.activity
  (:use [ciste.config :only [config]]
        [clojure.core.incubator :only [-?>>]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [validation-set presence-of acceptance-of]])
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
  (:import jiksnu.model.Activity))

(defonce page-size 20)
(def collection-name "activities")

(def create-probe (trace/probe-channel :activity:created))

(def create-validators
  (validation-set
   (presence-of   :_id)
   (presence-of   :id)
   (presence-of   :title)
   (presence-of   :author)
   (presence-of   :content)
   (acceptance-of :local         :accept (partial instance? Boolean))
   (acceptance-of :public        :accept (partial instance? Boolean))
   (presence-of   :update-source)
   (presence-of   [:object :object-type])
   (presence-of   :verb)
   (presence-of   :conversation)

   ;; TODO: These should be joda times
   (presence-of   :created)
   (presence-of   :updated)
   ))

(def set-field! (templates/make-set-field! collection-name))

(defn get-author
  [activity]
  (-> activity
      :author
      model.user/fetch-by-id))

(defn get-link
  [user rel content-type]
  (first (util/rel-filter rel (:links user) content-type)))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     ((templates/make-fetch-fn model/map->Activity collection-name)
      params options)))

(defn fetch-by-id
  [id]
  ;; TODO: Should this always take a string?
  (let [id (if (string? id) (util/make-id id) id)]
    (s/increment "activities fetched")
    (if-let [activity (mc/find-map-by-id collection-name id)]
      (model/map->Activity activity))))

(defn create
  [params]
  (let [errors (create-validators params)]
    (if (empty? errors)
      (do
        (log/debugf "Creating activity: %s" (pr-str params))
        (mc/insert collection-name params)
        (let [item (fetch-by-id (:_id params))]
          (trace/trace :activities:created item)
          (s/increment "activities created")
          item))
      (throw+ {:type :validation :errors errors}))))

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
  (if-let [activity (mc/find-one-as-map collection-name {:id id})]
    (model/map->Activity activity)))

(def delete        (templates/make-deleter collection-name))
(def drop!         (templates/make-dropper collection-name))
(def count-records (templates/make-counter collection-name))

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
