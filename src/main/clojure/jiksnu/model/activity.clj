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

(defn set-updated-time
  [activity]
  (if (:updated activity)
    activity
    (assoc activity :updated (sugar/date))))

(defn set-published-time
  [activity]
  (if (:published activity)
    activity
    (assoc activity :published (sugar/date))))

(defn set-actor
  [activity]
  (if-let [author (current-user-id)]
    (assoc activity :authors [author])))

(defn set-public
  [activity]
  (if (:public activity)
    activity
    (assoc activity :public true)))

(defn prepare-activity
  [activity]
  (-> activity
      set-id
      set-public
      set-published-time
      set-updated-time
      set-actor))

(defn create
  [activity]
  (if-let [prepared-activity (prepare-activity activity)]
    (entity/create Activity prepared-activity)))

(defn index
  "Return all the activities in the database as abdera entries"
  [& opts]
  (let [option-map (apply hash-map opts)]
    (entity/fetch Activity option-map :sort [(sugar/desc :updated)])))

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
           {} #_{:public true}))]
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
