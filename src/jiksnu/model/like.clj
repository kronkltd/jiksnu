(ns jiksnu.model.like
  (:use (ciste [debug :only [spy]])
        jiksnu.model)
  (:require (jiksnu.model [activity :as model.activity]
                          [user :as model.user])
            (karras [entity :as entity]
                    [sugar :as sugar]))
  (:import jiksnu.model.Like))

(defn drop!
  []
  (entity/delete-all Like))

(defn fetch-by-id
  [id]
  (entity/fetch-by-id Like id))

(defn delete
  [like]
  (entity/delete (fetch-by-id (:_id like))))

(defn create
  [options]
  (entity/create Like options))

;; FIXME: This is not quite right
(defn find-or-create
  [activity user]
  (create activity))

(defn fetch-all
  ([] (fetch-all {}))
  ([params & opts]
     (apply entity/fetch Like params opts)))

(defn fetch-by-user
  [user]
  (fetch-all {:user (:_id user)}))

(defn get-activity
  [like]
  (-> like :activity model.activity/fetch-by-id))

(defn get-actor
  [like]
  (-> like :user model.user/fetch-by-id))

(defn get-likes
  [activity]
  (seq (fetch-all {:activity (:_id activity)})))

(defn format-data
  "format a like for display in templates"
  [like]
  (let [user (model.user/fetch-by-id (:user like))]
    (:username user)))
