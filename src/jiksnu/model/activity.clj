(ns jiksnu.model.activity
  (:use [ciste.config :only [config]]
        [clojure.core.incubator :only [-?>>]]
        [jiksnu.model :only [map->Activity]]
        [jiksnu.session :only [current-user current-user-id is-admin?]]
        [jiksnu.transforms :only [set-_id set-created-time set-updated-time]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [validation-set presence-of acceptance-of]])
  (:require [clj-time.core :as time]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.string :as string]
            [jiksnu.abdera :as abdera]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            [monger.collection :as mc]
            [monger.query :as mq])
  (:import com.ocpsoft.pretty.time.PrettyTime
           java.util.Date
           jiksnu.model.Activity))

(defonce page-size 20)
(def collection-name "activities")

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

(defn get-author
  [activity]
  (-> activity
      :author
      model.user/fetch-by-id))

;; TODO: This operation should be performed on local posts. Remote
;; posts without an id should be rejected
(defn set-id
  [activity]
  (if (empty? (:id activity))
    (let [id (format "http://%s/notice/%s" (:domain (get-author activity)) (:_id activity))]
      (assoc activity :id id))
    activity))

(defn set-title
  [activity]
  (if (empty? (:title activity))
    ;; TODO: strip down to plain text and limit to 140 characters
    (assoc activity :title (:content activity))
    activity))

(defn get-link
  [user rel content-type]
  (first (model/rel-filter rel (:links user) content-type)))

(defn set-object-id
  [activity]
  (if (:id (:object activity))
    activity
    (assoc-in activity [:object :id] (abdera/new-id))))

(defn set-public
  [activity]
  (if (false? (:public activity))
    activity
    (assoc activity :public true)))

(defn set-remote
  [activity]
  (if (:local activity)
    activity
    (assoc activity :local false)))

(defn set-tags
  [activity]
  (let [tags (:tags activity )]
    (if (string? tags)
      (if (and tags (not= tags ""))
        (if-let [tag-seq (filter #(not= % "") (string/split tags #",\s*"))]
          (assoc activity :tags tag-seq)
          (dissoc activity :tags))
        (dissoc activity :tags))
      (if (coll? tags)
        activity
        (dissoc activity :tags)))))

(defn set-object-type
  [activity]
  (assoc-in
   activity [:object :object-type]
   (if-let [object-type (:object-type (:object activity))]
     (-> object-type
         ;; strip namespaces
         (string/replace #"http://onesocialweb.org/spec/1.0/object/" "")
         (string/replace #"http://activitystrea.ms/schema/1.0/" ""))
     "note")))

(defn set-parent
  [activity]
  (if (= (:parent activity) "")
    (dissoc activity :parent)
    activity))

(defn set-url
  [activity]
  (if (and (:local activity)
           (empty? (:url activity)))
    (assoc activity :url (str "http://" (config :domain) "/notice/" (:_id activity)))
    activity))

(defn prepare-activity
  [activity]
  (-> activity
      set-_id
      set-title
      set-object-id
      set-public
      set-remote
      set-tags
      set-created-time
      set-updated-time
      set-object-type
      set-parent
      set-url
      set-id
      ))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (let [sort-clause (mq/partial-query (mq/sort (:sort-clause options)))
           records (mq/with-collection collection-name
                     (mq/find params)
                     (merge sort-clause)
                     (mq/paginate :page (:page options 1)
                                  :per-page (:page-size options 20)))]
       (map map->Activity records))))

(defn fetch-by-id
  [id]
  ;; TODO: Should this always take a string?
  (let [id (if (string? id) (model/make-id id) id)]
    (if-let [activity (mc/find-map-by-id collection-name id)]
      (map->Activity activity))))

(defn create
  [activity]
  (let [activity (prepare-activity activity)
        errors (create-validators activity)]
    (if (empty? errors)
      (do
        (log/debugf "Creating activity: %s" (pr-str activity))
        (mc/insert collection-name activity)
        (fetch-by-id (:_id activity)))
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
  (mc/save collection-name activity))

(defn privacy-filter
  [user]
  (if user
    (if (not (is-admin? user))
      {:$or [{:public true}
             {:author (:_id user)}]})
    {:public true}))

(defn fetch-by-remote-id
  [id]
  (if-let [activity (mc/find-one-as-map collection-name {:id id})]
    (map->Activity activity)))

(defn drop!
  []
  (mc/remove collection-name))

(defn delete
  [activity]
  (mc/remove-by-id collection-name (:_id activity))
  activity)

(defn add-comment
  [parent comment]
  (mc/update collection-name
             {:_id (:_id parent)}
             {:$push {:comments (:_id comment)}}))

(defn set-object-updated
  [activity]
  (if (:updated (:object activity))
    activity
    (assoc-in activity [:object :updated] (time/now))))

(defn set-object-created
  [activity]
  (if (:created (:object activity))
    activity
    (assoc-in activity [:object :created] (time/now))))

(defn set-actor
  [activity]
  ;; TODO: Should we be allowing an author to be passed in?
  (if-let [author (or (:author activity)
                      (current-user-id))]
    (assoc activity :author author)))

(defn set-local
  [activity]
  (assoc activity :local true))

(defn parse-pictures
  [picture]
  (let [filename (:filename picture)
        tempfile (:tempfile picture)
        user-id (str (current-user-id))
        dest-file (io/file (str user-id "/" filename))]
    (when (and (not= filename "") tempfile)
      (.mkdirs (io/file user-id))
      (io/copy tempfile dest-file))))

(defn prepare-post
  [activity]
  (-> activity
      set-local
      set-updated-time
      set-object-updated
      set-object-created
      set-created-time
      set-actor))

(defn count-records
  ([] (count-records {}))
  ([params]
     (mc/count collection-name params)))
