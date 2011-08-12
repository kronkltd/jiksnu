(ns jiksnu.model.activity
  (:use ciste.debug
        jiksnu.model
        [jiksnu.session :only (current-user current-user-id is-admin?)])
  (:require [clojure.string :as string]
            [karras.entity :as entity]
            [karras.sugar :as sugar])
  (:import jiksnu.model.Activity))

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
