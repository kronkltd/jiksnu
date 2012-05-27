(ns jiksnu.model.like
  (:use [ciste.debug :only [spy]]
        jiksnu.model
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [karras.entity :as entity]
            [karras.sugar :as sugar])
  (:import jiksnu.model.Like))

(defn drop!
  []
  (entity/delete-all Like))

(defn fetch-by-id
  [id]
  (entity/fetch-by-id Like id))

(defn delete
  [like]
  (let [like (fetch-by-id (:_id like))]
    (entity/delete like)
    like))

(defn create
  [options]
  (if (:user options)
    (if (:activity options)
      (do
        (log/debug "Creating like")
        (entity/create Like options))
      (throw+ "Must contain an activity"))
    (throw+ "Must contain a user")))

;; TODO: get-like

;; FIXME: This is not quite right
(defn find-or-create
  [activity user]
  (create activity))

(defn fetch-all
  ([] (fetch-all {} {}))
  ([params opts]
     (apply entity/fetch-all Like params opts)))

;; TODO: use index to get pagination
(defn fetch-by-user
  [user]
  (fetch-all {:user (:_id user)}))

(defn get-activity
  [like]
  (-> like :activity model.activity/fetch-by-id))

(defn get-actor
  [like]
  (-> like :user model.user/fetch-by-id))

;; TODO: fetch-by-activity
(defn get-likes
  [activity]
  (seq (fetch-all {:activity (:_id activity)})))

(defn count-records
  ([] (count-records {}))
  ([params]
     (entity/count-instances Like params)))

;; TODO: deprecated
(defn format-data
  "format a like for display in templates"
  [like]
  (let [user (model.user/fetch-by-id (:user like))]
    (:username user)))
