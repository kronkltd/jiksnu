(ns jiksnu.model.activity
  (:use (ciste [debug :only (spy)])
        (clojure.contrib [core :only (-?> -?>>)])
        jiksnu.model
        [jiksnu.session :only (current-user current-user-id is-admin?)])
  (:require [clojure.string :as string]
            (jiksnu [abdera :as abdera])
            (jiksnu.model [user :as model.user])
            (karras [entity :as entity]
                    [sugar :as sugar]))
  (:import com.ocpsoft.pretty.time.PrettyTime
           jiksnu.model.Activity))

(defn make-activity
  [options]
  (entity/make Activity options))

(defn create
  [activity]
  (entity/create Activity activity))

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
  (let [user (current-user)
        option-map (apply hash-map opts)
        merged-options
        (merge
         {"object.object-type" {:$ne "comment"}}
         (privacy-filter user)
         option-map)]
    (entity/fetch Activity merged-options
                  :sort [(sugar/desc :published)]
                  :limit 20)))

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

(defn format-data
  [activity]
  (let [comments (map format-data (get-comments activity))
        actor (current-user)]
    {:id (str (:_id activity))
     :author (-> activity :author model.user/fetch-by-id
                 model.user/format-data)
     :object-type (-> activity :object :object-type)
     :local (:local activity)
     :public (:public activity)
     :content (or (-> activity :object :content)
                  (-> activity :content)
                  (-> activity :title))
     :title (or (-> activity :object :content)
                (:content activity)
                (:title activity))
     :lat (str (:lat activity))
     :long (str (:long activity))
     :authenticated (if-let [user (current-user)]
                      (model.user/format-data user))
     :tags []
     :uri (:uri activity)
     :recipients []
     :published (-?> activity :published format-date)
     :published-formatted (-?>> activity :published (.format (PrettyTime.)))
     :buttonable (and actor
                      (or (:admin actor)
                          (some #(= % (:authors activity)) actor)))
     :comment-count (str (count comments))
     :comments comments}))

(defn set-id
  [activity]
  (if (and (:id activity) (not= (:id activity) ""))
    activity
    (assoc activity :id (abdera/new-id))))

(defn set-object-id
  [activity]
  (if (:id (:object activity))
    activity
    (assoc-in activity [:object :id] (abdera/new-id))))

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

(defn set-public
  [activity]
  (if (false? (:public activity))
    activity
    (assoc activity :public true)))

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

(defn set-parent
  [activity]
  (if (= (:parent activity) "")
    (dissoc activity :parent)
    activity))

(defn set-object-type
  [activity]
  (assoc-in
   activity [:object :object-type]
   (if-let [object-type (:object-type (:object activity))]
     (-> object-type
         (string/replace #"http://onesocialweb.org/spec/1.0/object/" "")
         (string/replace #"http://activitystrea.ms/schema/1.0/" ""))
     "note")))

