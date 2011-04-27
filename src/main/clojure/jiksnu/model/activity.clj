(ns jiksnu.model.activity
  (:use ciste.debug
        jiksnu.model
        [jiksnu.session :only (current-user current-user-id is-admin?)])
  (:require [clojure.string :as string]
            [karras.entity :as entity]
            [karras.sugar :as sugar])
  (:import jiksnu.model.Activity))

(defn set-id
  [activity]
  (if (:_id activity)
    activity
    (assoc activity :_id (new-id))))

(defn set-object-id
  [activity]
  (if (:id (:object activity))
    activity
    (assoc-in activity [:object :id] (new-id))))

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
    (assoc activity :authors [author])))

(defn set-public
  [activity]
  (if (false? (:public activity))
    activity
    (assoc activity :public true)))

(defn set-tags
  [activity]
  (let [tags (:tags activity )]
    (if (and tags (not= tags ""))
      (if-let [tag-seq (filter #(not= % "") (string/split tags #",\s*"))]
        (assoc activity :tags tag-seq)
        (dissoc activity :tags))
      (dissoc activity :tags))))

(defn set-recipients
  [activity]
  (if-let [recipients (get activity "recipients")]
    (assoc activity :recipients
           (string/split recipients #",\s*"))
    activity))

(defn prepare-activity
  [activity]
  (-> activity
      set-id
      set-object-id
      set-public
      set-published-time
      set-tags
      set-recipients
      set-object-published
      set-updated-time
      set-object-updated
      set-actor))

(defn create-raw
  [activity]
  (entity/create Activity activity))

(defn update
  [activity]
  (entity/save activity))

(defn create
  [activity]
  (if-let [prepared-activity (prepare-activity activity)]
    (create-raw prepared-activity)))

(defn privacy-filter
  [user]
  (if user
    (if (not (is-admin? user))
      {:$or [{:public true}
             {:authors (:_id user)}]})
    {:public true}))

(defn index
  "Return all the activities in the database as abdera entries"
  [& opts]
  (let [user (current-user)
        option-map (apply hash-map opts)
        merged-options
        (merge
         #_{:$or [{:parent ""}
                {:parent {:$exists false}}]}
         (privacy-filter user)
         option-map)]
    (entity/fetch Activity merged-options
                  :sort [(sugar/desc :published)]
                  :limit 20)))

(defn fetch-by-id
  [id]
  (entity/fetch-one Activity {:_id id}))

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
  (index :authors (:_id user)))

(defn add-comment
  [parent comment]
  (entity/update Activity
                 (sugar/eq :_id (:_id parent))
                 (sugar/push :comments (:_id comment))))
