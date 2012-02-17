(ns jiksnu.model.activity
  (:use (ciste [debug :only [spy]])
        (clojure.core [incubator :only [-?> -?>>]])
        jiksnu.model
        [jiksnu.session :only [current-user current-user-id is-admin?]])
  (:require (clojure [string :as string])
            (clojure.java [io :as io])
            (clojure.tools [logging :as log])
            (jiksnu [abdera :as abdera])
            (jiksnu.actions [like-actions :as actions.like])
            (jiksnu.model [like :as model.like]
                          [user :as model.user])
            (karras [entity :as entity]
                    [sugar :as sugar]))
  (:import com.ocpsoft.pretty.time.PrettyTime
           jiksnu.model.Activity))

(defn make-activity
  [options]
  (entity/make Activity options))


;; TODO: This operation should be performed on local posts. Remote
;; posts without an id should be rejected
(defn set-id
  [activity]
  (if (and (:id activity) (not= (:id activity) ""))
    activity
    (assoc activity :id (abdera/new-id))))

(defn set-title
  [activity]
  (if (= (:title activity) "")
    (dissoc activity :title)
    activity))

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


(defn prepare-activity
  [activity]
  (-> activity
      set-id
      set-title
      set-object-id
      set-public
      set-remote
      set-tags
      set-object-type
      set-parent
      ))

(defn create
  [activity]
  (->> activity
       prepare-activity
       ;; make-activity
       ((fn [a]
          (log/debugf "Creating activity: %s" a)
           a))
       (entity/create Activity)))

(defn get-comments
  [activity]
  (entity/fetch Activity {:parent (:_id activity)}
                :sort [(sugar/asc :published)]))

(defn update
  [activity]
  (entity/save activity))

(defn privacy-filter
  [user]
  (if user
    (if (not (is-admin? user))
      {:$or [{:public true}
             {:author (:_id user)}]})
    {:public true}))

(defn index
  "Return all the activities in the database as abdera entries"
  [& opts]
  ;; TODO: move all this to action
  (let [user (current-user)
        option-map (apply hash-map opts)
        merged-options
        (merge
         ;; {"object.object-type" {:$ne "comment"}}
         (privacy-filter user)
         option-map)]
    (entity/fetch Activity merged-options
                  :sort [(sugar/desc :published)]
                  :limit 20)))

(defn fetch-all
  [& options]
  (apply entity/fetch Activity options))

(defn fetch-by-id
  [id]
  (entity/fetch-by-id Activity id))

(defn fetch-by-remote-id
  [id]
  (entity/fetch-one Activity {:remote-id id}))

(defn show
  [id]
  (let [user (current-user)
        options
        (merge
         {:_id id}
         (privacy-filter user))]
    (entity/fetch-one Activity options)))

(defn drop!
  []
  (entity/delete-all Activity))

(defn delete
  [activity]
  (entity/delete activity)
  activity)

(defn find-by-user
  [user]
  (index :author (:_id user)))

(defn add-comment
  [parent comment]
  (entity/update Activity
                 (sugar/eq :_id (:_id parent))
                 (sugar/push :comments (:_id comment))))

(defn prettyify-time
  [date]
  (-?>> date (.format (PrettyTime.))))

(defn set-updated-time
  [activity]
  (if (:updated activity)
    activity
    (assoc activity :updated (sugar/date))))

(defn set-object-updated
  [activity]
  (if (:updated (:object activity))
    activity
    (assoc-in activity [:object :updated] (sugar/date))))

(defn set-published-time
  [activity]
  (if (:published activity)
    activity
    (assoc activity :published (sugar/date))))

(defn set-object-published
  [activity]
  (if (:published (:object activity))
    activity
    (assoc-in activity [:object :published] (sugar/date))))

(defn set-actor
  [activity]
  (if-let [author (current-user-id)]
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
      set-object-published
      set-published-time
      set-actor))

