(ns jiksnu.model.activity
  (:use jiksnu.model
        [jiksnu.session :only (current-user current-user-id is-admin?)])
  (:require [karras.entity :as entity]
            [karras.sugar :as sugar])
  (:import jiksnu.model.Activity
           org.apache.axiom.util.UIDGenerator))

(defn new-id
  []
  (UIDGenerator/generateURNString))

(defn set-id
  [activity]
  (if (:_id activity)
    activity
    (assoc activity :_id (new-id))))

(defn set-object-id
  [activity]
  (if (:object-id activity)
    activity
    (assoc activity :object-id (new-id))))

(defn set-updated-time
  [activity]
  (if (:updated activity)
    activity
    (assoc activity :updated (sugar/date))))

(defn set-object-updated
  [activity]
  (if (:object-updated activity)
    activity
    (assoc activity :object-updated (sugar/date))))

(defn set-published-time
  [activity]
  (if (:published activity)
    activity
    (assoc activity :published (sugar/date))))

(defn set-object-published
  [activity]
  (if (:object-published activity)
    activity
    (assoc activity :object-published (sugar/date))))

(defn set-actor
  [activity]
  (if-let [author (current-user-id)]
    (assoc activity :authors [author])))

(defn set-public
  [activity]
  (if (false? (:public activity))
    activity
    (assoc activity :public true)))

(defn prepare-activity
  [activity]
  (-> activity
      set-id
      set-object-id
      set-public
      set-published-time
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

(defn index
  "Return all the activities in the database as abdera entries"
  [& opts]
  (let [user (current-user)]
    (let [option-map (apply hash-map opts)]
      (let [merged-options
            (merge
             {:parent {:$exists false}}
             (if user
               (if (not (is-admin? user))
                 {:$or [{:public true}
                        {:authors (:_id user)}]})
               {:public true})
             option-map)]
        (entity/fetch Activity merged-options
                      :sort [(sugar/desc :published)])))))

(defn fetch-by-id
  [id]
  (entity/fetch-one Activity {:_id id}))

(defn show
  [id]
  (let [user (current-user)
        options
        (merge
         {:_id id}
         (if user
           (if (not (is-admin? user))
             {:$or [{:public true}
                    {:authors (:_id user)}]})
           {:public true}))]
    (entity/fetch-one Activity options)))

(defn drop!
  []
  (entity/delete-all Activity))

(defn delete
  [id]
  (entity/delete (show id)))

(defn find-by-user
  [user]
  (index :authors (:_id user)))

(defn add-comment
  [parent comment]
  (entity/update Activity
                 (sugar/eq :_id (:_id parent))
                 (sugar/push :comments (:_id comment))
                 )
  )
